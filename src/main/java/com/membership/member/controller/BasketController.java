package com.membership.member.controller;

import com.membership.member.Service.BasketService;
import com.membership.member.entity.*;
import com.membership.member.repository.*;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class BasketController {
    private final BasketService baskets;
    private final CoffeeRepository coffees;
    private final MemberRepository members;
    private final CoffeeOrderRepository orders;
    public BasketController(BasketService baskets, CoffeeRepository coffees, MemberRepository members,
            CoffeeOrderRepository orders) {
        this.baskets=baskets; this.coffees=coffees; this.members=members; this.orders=orders;
    }
    @SuppressWarnings("unchecked")
    private Map<Long,Integer> basket(HttpSession session, Principal principal) {
        if (!principal.getName().equals(session.getAttribute("basketOwner"))) {
            session.setAttribute("basketOwner", principal.getName());
            session.setAttribute("basket", new LinkedHashMap<Long,Integer>());
            session.setAttribute("checkoutKey", UUID.randomUUID().toString());
        }
        return (Map<Long,Integer>) session.getAttribute("basket");
    }
    @GetMapping("/basket")
    public String view(HttpSession session, Principal principal, Model model) {
        synchronized (session) {
            Map<Long,Integer> basket=basket(session,principal);
            boolean removed=basket.keySet().removeIf(id -> !coffees.existsById(id));
            if (removed) session.setAttribute("checkoutKey",UUID.randomUUID().toString());
            List<OrderLine> items=baskets.items(basket);
            Long memberId=members.findByEmail(principal.getName()).orElseThrow().getId();
            Optional<CoffeeOrder> placed=orders.findByCheckoutKeyAndMemberId(
                    (String)session.getAttribute("checkoutKey"),memberId);
            model.addAttribute("alreadyOrdered",placed.isPresent());
            model.addAttribute("receiptUrl",placed.map(o -> "/orders/"+o.getId()).orElse(""));
            model.addAttribute("items",items);
            model.addAttribute("empty",items.isEmpty());
            model.addAttribute("removedItems",removed);
            model.addAttribute("total",items.stream().mapToLong(OrderLine::getSubtotal).sum());
            model.addAttribute("checkoutKey",session.getAttribute("checkoutKey"));
        }
        return "coffee/basket";
    }
    @PostMapping("/basket/add")
    public String add(@RequestParam("id") Long id, @RequestParam(defaultValue="1", name="quantity") int quantity,
            HttpSession session, Principal principal) {
        if (!coffees.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        synchronized (session) {
            Map<Long,Integer> basket=basket(session,principal);
            int total=basket.getOrDefault(id,0)+quantity;
            if (quantity<1 || quantity>99 || total>99 || basket.size()>=100 && !basket.containsKey(id))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose 1 to 99 of each coffee.");
            basket.put(id,total);
            session.setAttribute("checkoutKey",UUID.randomUUID().toString());
        }
        return "redirect:/basket";
    }
    @PostMapping("/basket/update")
    public String update(@RequestParam("id") Long id, @RequestParam("quantity") int quantity,
            HttpSession session, Principal principal) {
        if(quantity<1 || quantity>99) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        synchronized(session) {
            Map<Long,Integer> basket=basket(session,principal);
            if (!basket.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            if (!basket.get(id).equals(quantity)) {
                basket.put(id,quantity);
                session.setAttribute("checkoutKey",UUID.randomUUID().toString());
            }
        }
        return "redirect:/basket";
    }
    @PostMapping("/basket/remove")
    public String remove(@RequestParam("id") Long id, HttpSession session, Principal principal) {
        synchronized(session) {
            if (basket(session,principal).remove(id) != null)
                session.setAttribute("checkoutKey",UUID.randomUUID().toString());
        }
        return "redirect:/basket";
    }
    @PostMapping("/basket/clear")
    public String clear(HttpSession session, Principal principal) {
        synchronized(session) {
            basket(session,principal).clear();
            session.setAttribute("checkoutKey",UUID.randomUUID().toString());
        }
        return "redirect:/basket";
    }
    @PostMapping("/basket/checkout")
    public String checkout(@RequestParam("checkoutKey") String key, HttpSession session, Principal principal) {
        synchronized(session) {
            Map<Long,Integer> basket=basket(session,principal);
            Long memberId=members.findByEmail(principal.getName()).orElseThrow().getId();
            Optional<CoffeeOrder> previous=orders.findByCheckoutKeyAndMemberId(key,memberId);
            if(previous.isPresent()) return "redirect:/orders/"+previous.get().getId();
            if (!key.equals(session.getAttribute("checkoutKey"))) throw new ResponseStatusException(HttpStatus.CONFLICT);
            CoffeeOrder order=baskets.checkout(memberId,key,basket);
            // Keep the selection and checkout key so revisiting cannot create a duplicate order.
            return "redirect:/orders/"+order.getId();
        }
    }
    @GetMapping("/orders/{id}")
    public String receipt(@PathVariable("id") Long id, Principal principal, Model model) {
        CoffeeOrder order=orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long memberId=members.findByEmail(principal.getName()).orElseThrow().getId();
        if (!order.getMemberId().equals(memberId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("order",order);
        return "coffee/order";
    }
}
