package com.membership.member.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;
    @Test
    @DisplayName("retrieve all reviews on a particular coffee")
    void findByCoffeeId() {

        // goal: retrieve all reviews on coffee Latte


    }
}