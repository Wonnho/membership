package com.membership.member.dto;

import com.membership.member.entity.Coffee;
import com.membership.member.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    private Long id;
    private Long coffeeId;
    private String username;
    private String review;

    public static ReviewDto ReviewDTO(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getCoffee().getId(),
                r.getMember().getUsername(),
                r.getReview()
        );

    }
}
