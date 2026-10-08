package com.membership.member.api;

import com.membership.member.Service.ReviewService;
import com.membership.member.dto.ReviewDto;
import com.membership.member.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;
    //1. retrieve Review
    @GetMapping("/api/coffee/{coffeeId}/reviews")
    public ResponseEntity<List<ReviewDto>> review(@PathVariable("coffeeId") Long coffeeId) {
        List<ReviewDto>  reviewDto=reviewService.getReviews(coffeeId);
        return ResponseEntity.status(HttpStatus.OK).body(reviewDto);
    }

    //2.create review
    @PostMapping("/api/coffee/{coffeeId}/reviews")
    public ResponseEntity<ReviewDto> createReview(@PathVariable("coffeeId") Long coffeeId,
                                  @RequestBody ReviewDto reviewDto) {

       ReviewDto  created=reviewService.createReview(coffeeId,reviewDto);
        return ResponseEntity.status(HttpStatus.OK).body(created);

    }
    //3. update review
    // 4. dlete review

}
