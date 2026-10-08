package com.membership.member.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Review { // review on a coffee

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="coffee_id") // foreign key: maps this key to id of Coffee
    private Coffee coffee;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;  //writer who comments a coffee

    @Column
    private String review; // write a review about a coffee


    /**
     * Creates a new, unsaved review for the supplied coffee and member.
     * The database assigns the review ID when the entity is saved.
     */
    public static Review create(Coffee coffee, Member member, String reviewText) {
        // A review must always belong to an existing coffee.
        if (coffee == null) {
            throw new IllegalArgumentException("Coffee is required to create a review");
        }

        // A review must always have an author.
        if (member == null) {
            throw new IllegalArgumentException("Member is required to create a review");
        }

        // Reject null, empty, and whitespace-only review content.
        if (reviewText == null || reviewText.isBlank()) {
            throw new IllegalArgumentException("Review text is required");
        }

        // Build the entity. Its generated ID remains null until save().
        Review review = new Review();
        review.coffee = coffee;
        review.member = member;
        review.review = reviewText.trim();

        return review;
    }
}
