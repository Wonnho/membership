package com.membership.member.repository;

import com.membership.member.entity.Coffee;
import com.membership.member.entity.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    @DisplayName("retrieve all reviews on a particular coffee")
    void findByCoffeeId() {

        // goal: retrieve all reviews on coffee Latte

        //1. prepare input data
        Long coffeeId = 2L; //Latte
        // 2. actual data
        List<Review> actual = reviewRepository.findByCoffeeId(coffeeId);

        // 3. expected data
        Coffee coffee = new Coffee(2L, "Latte", 5000, "/images/coffee/coffee-latte.jpg");
        Review review1 = new Review();
        review1.setId(4L);
        review1.setCoffee(coffee);
        review1.setReview("Creamy and well balanced.");

        Review review2 = new Review();
        review2.setId(5L);
        review2.setCoffee(coffee);
        review2.setReview("The espresso and milk work perfectly together.");

        Review review3 = new Review();
        review3.setId(6L);
        review3.setCoffee(coffee);
        review3.setReview("Soft, smooth, and easy to drink.");


        // 4. compare and verify
        //assertEquals(expected.toString(),actual.toString());

        List<Review> expected = List.of(review1, review2, review3);

// 4. Compare and verify
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            Review expectedReview = expected.get(i);
            Review actualReview = actual.get(i);

            assertAll(
                    () -> assertEquals(
                            expectedReview.getId(),
                            actualReview.getId()
                    ),
                    () -> assertEquals(
                            expectedReview.getReview(),
                            actualReview.getReview()
                    ),
                    () -> assertEquals(
                            expectedReview.getCoffee().getId(),
                            actualReview.getCoffee().getId()
                    ),
                    () -> assertEquals(
                            expectedReview.getCoffee().getCoffee(),
                            actualReview.getCoffee().getCoffee()
                    ),
                    () -> assertEquals(
                            expectedReview.getCoffee().getPrice(),
                            actualReview.getCoffee().getPrice()
                    ),
                    () -> assertEquals(
                            expectedReview.getCoffee().getImage(),
                            actualReview.getCoffee().getImage()
                    )
            );
        }
    }

    @Test
    @DisplayName("Retrieve all reviews written by a particular user")
    void findByUsername() {
        //1. prepare input data
        String username = "Grok";

        // 2. Actual data
        List<Review> actual =
                reviewRepository.findByUsername(username);

        actual.forEach(review -> {
            System.out.println(
                    "Username: " + review.getMember().getUsername()
            );
            System.out.println(
                    "Review: " + review.getReview()
            );
        });

        // 3. expected data

        assertFalse(actual.isEmpty());

        assertTrue(
                actual.stream().allMatch(review ->
                        review.getMember()
                                .getEmail()
                                .toLowerCase()
                                .startsWith(username.toLowerCase() + "@")
                )
        );
    }
}