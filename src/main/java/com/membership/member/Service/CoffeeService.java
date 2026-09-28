package com.membership.member.Service;

import com.membership.member.entity.Coffee;
import com.membership.member.repository.CoffeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CoffeeService {
    @Autowired
    private CoffeeRepository coffeeRepository;

    public List<Coffee> retrieveAll() {

       List<Coffee> coffees=(ArrayList<Coffee>) coffeeRepository.findAll();
        return coffees;
    }

    public Optional<Coffee> retrieveCoffeeById(Long id) {
        log.debug("Retrieving coffee from repository: id={}", id);

        return coffeeRepository.findById(id);

    }

    public Coffee createCoffee(Coffee coffee) {
        log.debug("show coffee created from repository: coffee={}",coffee);
      return coffeeRepository.save(coffee);
    }
}
