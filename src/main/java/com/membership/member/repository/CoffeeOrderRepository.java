package com.membership.member.repository;
import com.membership.member.entity.CoffeeOrder;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
public interface CoffeeOrderRepository extends CrudRepository<CoffeeOrder,Long> {
    Optional<CoffeeOrder> findByCheckoutKeyAndMemberId(String checkoutKey, Long memberId);
}
