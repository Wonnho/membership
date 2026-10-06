package com.membership.member.api;

import com.membership.member.Service.ReviewService;
import com.membership.member.dto.CoffeeDto;
import com.membership.member.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;
    //1. retrieve Review
    @GetMapping("/api/coffee/{coffeeId}/reviews")
    public List<Review> review(@PathVariable("coffeeId") Long coffeeId) {
          return reviewService.getReviews(coffeeId);
    }
    //2.create review
    //3. update review
    // 4. dlete review

}
