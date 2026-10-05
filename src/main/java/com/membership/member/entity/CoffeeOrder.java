package com.membership.member.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;
@Entity @Getter @NoArgsConstructor
public class CoffeeOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    @Column(unique=true, nullable=false)
    private String checkoutKey;
    private Instant createdAt;
    private long total;
    @ElementCollection(fetch=FetchType.EAGER)
    @OrderColumn(name="line_number")
    private List<OrderLine> items = new ArrayList<>();
    public CoffeeOrder(Long memberId, String checkoutKey, List<OrderLine> items) {
        this.memberId=memberId; this.checkoutKey=checkoutKey; this.items=new ArrayList<>(items);
        this.createdAt=Instant.now();
        this.total=items.stream().mapToLong(OrderLine::getSubtotal).sum();
    }
}
