package com.membership.member.repository;

import com.membership.member.entity.Coffee;
import com.membership.member.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    @Query(value="select * from review where coffee_id=:coffeeId",nativeQuery = true)
    List<Review> findByCoffeeId(Long coffeeId);
}
