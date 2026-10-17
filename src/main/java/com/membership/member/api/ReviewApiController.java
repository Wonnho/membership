package com.membership.member.api;

import com.membership.member.Service.ReviewService;
import com.membership.member.dto.ReviewDto;
import com.membership.member.dto.ReviewForm;
import com.membership.member.entity.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
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
    public String createReview(
            @PathVariable("coffeeId") Long coffeeId,
            @ModelAttribute ReviewForm form,
            Authentication authentication) {

//       ReviewDto  created=reviewService.createReview(coffeeId,form);
//        return ResponseEntity.status(HttpStatus.OK).body(created);

//        ReviewDto created = reviewService.createReview(
//               coffeeId,
//                authentication.getName(),
//        );

        reviewService.createReview(
                coffeeId,
                authentication.getName(),
                form
        );


//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(created);
        return "redirect:/coffee/read/" + coffeeId + "#reviews";


    }

    //3. update review
    @PatchMapping("/api/coffee/{coffeeId}/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> patchReview(@PathVariable("coffeeId") Long coffeeId,
                                                 @PathVariable("reviewId") Long reviewId,
                                                 @RequestBody ReviewDto reviewDto) {
        ReviewDto patched=reviewService.patchReview(coffeeId,reviewId,reviewDto);
        return ResponseEntity.status(HttpStatus.OK).body(patched);

    }
    // 4. delete review
    @DeleteMapping("/api/coffee/{coffeeId}/reviews/{reviewId}")
    public void deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
        log.info("delete reviewId :" +reviewId);
//        ReviewDto   deleted=reviewService.deleteReview(reviewId);
    //      return ResponseEntity.status(HttpStatus.OK).body(deleted);
    }
}
