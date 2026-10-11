package com.membership.member.controller;

import com.membership.member.Service.CoffeeImageService;
import com.membership.member.Service.CoffeeService;
import com.membership.member.Service.ReviewService;
import com.membership.member.entity.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Controller
@Slf4j
public class CoffeePageController {

    private final CoffeeService coffeeService;
    private final CoffeeImageService coffeeImages;

    private final ReviewService reviewService;

    public CoffeePageController(CoffeeService coffeeService, CoffeeImageService coffeeImages,
                                ReviewService reviewService) {
        this.coffeeService = coffeeService;
        this.coffeeImages = coffeeImages;
        this.reviewService=reviewService;
    }

    @GetMapping("/coffee")
    public String coffeeIndex(Model model) {
        List<Coffee> coffees = coffeeService.retrieveAll();

        log.info("Rendering coffee index with {} items", coffees.size());
        model.addAttribute("coffees", coffees);

        return "coffee/index";
    }

    @GetMapping({"/coffee/read", "/coffeeboard/read"})
    public String coffeeList(@RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        List<Coffee> coffees = coffeeService.search(query);
        coffees.sort(Comparator.comparing(Coffee::getId));
        int pages = Math.max(1, (coffees.size() + 8) / 9);
        int current = Math.max(0, Math.min(page, pages - 1));
        model.addAttribute("coffees", coffees.subList(current * 9, Math.min(current * 9 + 9, coffees.size())));
        model.addAttribute("pageNumber", current + 1);
        model.addAttribute("pageCount", pages);
        model.addAttribute("hasPrevious", current > 0);
        model.addAttribute("hasNext", current + 1 < pages);
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        model.addAttribute("previousUrl", "/coffee/read?q=" + encoded + "&page=" + (current - 1));
        model.addAttribute("nextUrl", "/coffee/read?q=" + encoded + "&page=" + (current + 1));
        model.addAttribute("searchActive", !query.isBlank());
        model.addAttribute("resultCount", coffees.size());
        return "coffeeboard/read";
    }

    @GetMapping("/coffee/read/{id}")
    public String readCoffee(@PathVariable("id") Long id, Model model) {
        Coffee coffee = coffeeService.retrieveCoffeeById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coffee not found: " + id));
        model.addAttribute("coffee", coffee);
        model.addAttribute("reviewDtos", reviewService.getReviews(id));
        return "coffeeboard/detail";
    }

    @GetMapping("/coffeeboard/register")
    public String registerCoffeeForm(Model model) {
        model.addAttribute("imageValue", "");
        return "coffeeboard/register";
    }

    @PostMapping("/coffeeboard/register")
    public String registerCoffee(
            @RequestParam("coffee") String name,
            @RequestParam("price") Integer price,
            @RequestParam("image") String image) {
        if (name.isBlank() || name.trim().length() > 255 || price == null || price < 0
                || !coffeeImages.isValid(image)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a coffee name, a non-negative price, and choose an available image.");
        }
        Coffee coffee = new Coffee();
        coffee.setCoffee(name.trim());
        coffee.setPrice(price);
        coffee.setImage(image);
        Coffee savedCoffee = coffeeService.createCoffee(coffee);
        return "redirect:/coffee/read/" + savedCoffee.getId();
    }

    @GetMapping("/coffeeboard/modify/{id}")
    public String modifyForm(@PathVariable("id") Long id, Model model) {
        Coffee coffee = coffeeService.retrieveCoffeeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("coffee", coffee);
        model.addAttribute("imageValue", coffee.getImage() == null ? "" : coffee.getImage());
        return "coffeeboard/modify";
    }

    @PostMapping("/coffeeboard/modify/{id}")
    public String modifyCoffee(@PathVariable("id") Long id,
            @RequestParam("coffee") String name, @RequestParam("price") Integer price,
            @RequestParam("image") String image) {
        if (name.isBlank() || name.trim().length() > 255 || price == null || price < 0
                || !coffeeImages.isValid(image)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check the name, price and image.");
        }
        coffeeService.updateCoffee(id, name.trim(), price, image);
        return "redirect:/coffee/read";
    }

    @PostMapping("/coffeeboard/remove/{id}")
    public String removeCoffee(@PathVariable("id") Long id) {
        coffeeService.deleteCoffee(id);
        return "redirect:/coffee/read";
    }
}
