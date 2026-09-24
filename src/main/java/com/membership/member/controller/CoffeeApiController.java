package com.membership.member.controller;

import com.membership.member.entity.Coffee;
import com.membership.member.repository.CoffeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CoffeeApiController {

    @Autowired
    private CoffeeRepository coffeeRepository;

    @GetMapping("/api/coffee")
    public List<Coffee> retrieveCoffee() {
        // retrieve all coffee data
    ArrayList<Coffee> coffee= (ArrayList<Coffee>) coffeeRepository.findAll();

        return coffee;
    }

    @GetMapping("/api/coffee/{id}")
    public ResponseEntity<Coffee> retrieveCoffeeById(@PathVariable("id") Long id) {
        // retrieve a coffee
        Coffee coffee =coffeeRepository.findById(id).orElse(null);

        return coffee !=null?
              ResponseEntity.status(HttpStatus.OK).body(coffee):
              ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
