package com.membership.member.Service;

import com.membership.member.entity.Coffee;
import com.membership.member.entity.Review;
import com.membership.member.repository.CoffeeRepository;
import com.membership.member.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CoffeeRepository coffeeRepository;

    public List<Review> getReviews(Long coffeeId) {
      Coffee coffee=coffeeRepository.findById(coffeeId).orElseThrow(()->new IllegalArgumentException("No such a coffee in Menu"));

      return reviewRepository.findByCoffeeId(coffeeId);
    }
}
