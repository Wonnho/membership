package com.membership.member.controller;

import com.membership.member.Service.ReviewService;
import com.membership.member.dto.ReviewDto;
import com.membership.member.dto.ReviewForm;
import com.membership.member.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ReviewPageController {

//    @Autowired
    private ReviewService reviewService;

    public ReviewPageController(ReviewService reviewService) {
        this.reviewService = reviewService;
 }

    @PostMapping("/coffee/read/{coffeeId}/reviews")
    public String createReview(
            @PathVariable("coffeeId") Long coffeeId,
            @ModelAttribute ReviewForm form,
            Authentication authentication) {

        reviewService.createReview(
                coffeeId,
                authentication.getName(),
                form
        );

        return "redirect:/coffee/read/" + coffeeId + "#reviews";
    }

}
