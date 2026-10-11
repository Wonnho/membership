package com.membership.member.Service;

import com.membership.member.dto.ReviewDto;
import com.membership.member.entity.Coffee;
import com.membership.member.entity.Member;
import com.membership.member.entity.Review;
import com.membership.member.repository.CoffeeRepository;
import com.membership.member.repository.MemberRepository;
import com.membership.member.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CoffeeRepository coffeeRepository;

    @Autowired
    private MemberRepository memberRepository;

    public List<ReviewDto> getReviews(Long coffeeId) {
        // 1. retrieve review
        List<Review> reviewId = reviewRepository.findAllByCoffee_Id(coffeeId);

//      return reviewId.stream()
//                .map(review -> new ReviewDto(
//                        review.getId(),
//                        review.getCoffee().getId(),
//                        review.getMember().getUsername(),
//                        review.getReview()
//                ))
//                .toList();

        //2. transform entity to DTO
        List<ReviewDto> reviewDtos = new ArrayList<>();

        for (int k = 0; k < reviewId.size(); k++) {
            Review r = reviewId.get(k);
            ReviewDto dto = ReviewDto.ReviewDTO(r);
            reviewDtos.add(dto);
        }
        return reviewDtos;
    }

    @Transactional
    public ReviewDto createReview(Long coffeeId, ReviewDto reviewDto) {
        // 1. find the coffee (throws if missing)
        Coffee coffee = coffeeRepository.findById(coffeeId)
                .orElseThrow(() -> new IllegalArgumentException("No such a coffee in Menu"));


        // 2. find the member
        Member member = memberRepository.findById(reviewDto.getId()).orElseThrow(() ->
                new IllegalArgumentException("No such a member" + reviewDto.getId()));

        // 3. Create a new unsaved Review entity
        Review review=Review.create(
                coffee,
                member,
                reviewDto.getReview()
        );

        // 4. Insert it into the database.
      Review  saved=reviewRepository.save(review);


        return reviewDto.ReviewDTO(saved);
    }

    @Transactional
    public ReviewDto patchReview(Long coffeeId,Long reviewId, ReviewDto reviewDto) {
        // 1. Find the existing review.
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Review not found: " + reviewId
                        ));

        // 2. Confirm that the review belongs to the URL's coffee.
        if (!review.getCoffee().getId().equals(coffeeId)) {
            throw new IllegalArgumentException(
                    "Review " + reviewId
                            + " does not belong to coffee " + coffeeId
            );
        }

        // 3. Change only the review text.
        review.patchReview(reviewDto.getReview());


        // 4. Save the existing entity.
        Review savedReview = reviewRepository.save(review);

        // 5. Convert the result to a DTO.

        return reviewDto.ReviewDTO(review);
    }


    @Transactional
    public void deleteReview(Long reviewId) {
    reviewRepository.deleteById(reviewId);

    }
}
