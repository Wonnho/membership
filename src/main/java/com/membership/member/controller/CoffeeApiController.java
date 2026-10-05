package com.membership.member.controller;

import com.membership.member.Service.CoffeeService;
import com.membership.member.dto.CoffeeDto;
import com.membership.member.entity.Coffee;
import com.membership.member.repository.CoffeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class CoffeeApiController {

    @Autowired
    private com.membership.member.Service.CoffeeImageService coffeeImages;

    @Autowired
    private CoffeeService coffeeService;

    @GetMapping("/api/coffee")
    @ResponseBody
    public ResponseEntity<List<Coffee>> retrieveCoffee() {
        log.info("GET /api/coffee requested");

        // retrieve all coffee data
          List<Coffee>   coffees=coffeeService.retrieveAll();

        log.info("Retrieved {} coffee records", coffees.size());
        log.debug("Retrieved coffees: {}", coffees);

        return ResponseEntity.status(HttpStatus.OK).body(coffees);
    }

    @GetMapping("/api/coffee/{id}")
    @ResponseBody
    public ResponseEntity<Coffee> retrieveCoffeeById(@PathVariable("id") Long id) {
        log.info("GET /api/coffee/{} requested", id);

        // retrieve a particular coffee
        Optional<Coffee> coffee =coffeeService.retrieveCoffeeById(id);

        return coffee
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping("/api/coffee")
    @ResponseBody
    public ResponseEntity<Coffee> createCoffee(
            @RequestBody Coffee coffee) {

        log.info(
                "Coffee creation requested: name={}, price={}",
                coffee.getCoffee(),
                coffee.getPrice()
        );

        Coffee savedCoffee = coffeeService.createCoffee(coffee);

        log.info(
                "Coffee created successfully: id={}, name={}",
                savedCoffee.getId(),
                savedCoffee.getCoffee()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCoffee);
    }

    @PatchMapping("/api/coffee/{id}")
    @ResponseBody
    public Coffee patch(@PathVariable("id") Long id,@RequestBody CoffeeDto coffeeDto) {


        return coffeeService.patchCoffee(id,coffeeDto);
    }

    @DeleteMapping("/api/coffee/{id}")
    @ResponseBody
    public void remove(@PathVariable("id") Long id) {


        coffeeService.deleteCoffee(id);
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
        coffees.sort(java.util.Comparator.comparing(Coffee::getId));
        int pages = Math.max(1, (coffees.size() + 8) / 9);
        int current = Math.max(0, Math.min(page, pages - 1));
        model.addAttribute("coffees", coffees.subList(current * 9, Math.min(current * 9 + 9, coffees.size())));
        model.addAttribute("pageNumber", current + 1);
        model.addAttribute("pageCount", pages);
        model.addAttribute("hasPrevious", current > 0);
        model.addAttribute("hasNext", current + 1 < pages);
        String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
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

    @PostMapping("/coffeeboard/upload")
    @ResponseBody
    public java.util.Map<String, String> uploadCoffeeImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        return java.util.Map.of("image", coffeeImages.upload(file));
    }

    @GetMapping("/coffee-images/{name}")
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> coffeeImage(@PathVariable("name") String name) {
        java.nio.file.Path path = coffeeImages.resolve(name);
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .contentType(name.endsWith(".png") ? org.springframework.http.MediaType.IMAGE_PNG
                        : org.springframework.http.MediaType.IMAGE_JPEG)
                .body(new org.springframework.core.io.FileSystemResource(path));
    }

}
