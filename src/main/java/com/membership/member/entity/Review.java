package com.membership.member.entity;


import jakarta.persistence.*;

@Entity
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

}
