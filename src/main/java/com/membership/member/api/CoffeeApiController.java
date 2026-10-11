package com.membership.member.api;

import com.membership.member.Service.CoffeeService;
import com.membership.member.dto.CoffeeDto;
import com.membership.member.entity.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class CoffeeApiController {

    private final CoffeeService coffeeService;

    public CoffeeApiController(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }

    @GetMapping("/api/coffee")
    public ResponseEntity<List<Coffee>> retrieveCoffee() {
        log.info("GET /api/coffee requested");

        // retrieve all coffee data
          List<Coffee>   coffees=coffeeService.retrieveAll();

        log.info("Retrieved {} coffee records", coffees.size());
        log.debug("Retrieved coffees: {}", coffees);

        return ResponseEntity.status(HttpStatus.OK).body(coffees);
    }

    @GetMapping("/api/coffee/{id}")
    public ResponseEntity<Coffee> retrieveCoffeeById(@PathVariable("id") Long id) {
        log.info("GET /api/coffee/{} requested", id);

        // retrieve a particular coffee
        Optional<Coffee> coffee =coffeeService.retrieveCoffeeById(id);

        return coffee
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping("/api/coffee")
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
    public Coffee patch(@PathVariable("id") Long id,@RequestBody CoffeeDto coffeeDto) {


        return coffeeService.patchCoffee(id,coffeeDto);
    }

    @DeleteMapping("/api/coffee/{id}")
    public void remove(@PathVariable("id") Long id) {


        coffeeService.deleteCoffee(id);
    }
}
