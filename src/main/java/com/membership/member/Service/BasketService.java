package com.membership.member.Service;

import com.membership.member.entity.*;
import com.membership.member.repository.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BasketService {
    private final CoffeeRepository coffees;
    private final CoffeeOrderRepository orders;
    public BasketService(CoffeeRepository coffees, CoffeeOrderRepository orders) {
        this.coffees=coffees; this.orders=orders;
    }
    public List<OrderLine> items(Map<Long,Integer> basket) {
        List<OrderLine> lines = new ArrayList<>();
        for (var entry : basket.entrySet()) {
            Coffee coffee = coffees.findById(entry.getKey()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.CONFLICT, "A coffee was removed. Clear your basket and choose again."));
            if (coffee.getPrice() == null || coffee.getPrice() < 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This coffee has no valid price.");
            lines.add(new OrderLine(coffee.getId(), coffee.getCoffee(), coffee.getPrice(), entry.getValue()));
        }
        return lines;
    }
    @Transactional
    public CoffeeOrder checkout(Long memberId, String key, Map<Long,Integer> basket) {
        Optional<CoffeeOrder> previous=orders.findByCheckoutKeyAndMemberId(key, memberId);
        if (previous.isPresent()) return previous.get();
        if (basket.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your basket is empty.");
        return orders.save(new CoffeeOrder(memberId, key, items(basket)));
    }
}
