package com.membership.member.entity;
import jakarta.persistence.*;
import lombok.*;
@Embeddable @Getter @NoArgsConstructor
public class OrderLine {
    private Long coffeeId;
    private String coffeeName;
    private int unitPrice;
    private int quantity;
    public OrderLine(Long coffeeId, String coffeeName, int unitPrice, int quantity) {
        this.coffeeId=coffeeId; this.coffeeName=coffeeName; this.unitPrice=unitPrice; this.quantity=quantity;
    }
    public long getSubtotal() { return (long) unitPrice * quantity; }
}
