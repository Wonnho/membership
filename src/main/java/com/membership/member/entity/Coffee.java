package com.membership.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Coffee {

    @Id
    private Long id;

    private String coffee;

    private int price;
}
