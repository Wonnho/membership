package com.membership.member.repository;

import com.membership.member.entity.Coffee;
import com.membership.member.entity.Member;
import com.membership.member.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    @Query(value="select * from review where coffee_id=:coffeeId",nativeQuery = true)
    List<Review> findByCoffeeId(Long coffeeId);

    // retrieve all reviews by a particular NickName
   //  List<Member> findByUsername(String username);
   // @Query(name = "Review.findByUsername", nativeQuery = true)
    List<Review> findByUsername(
            @Param("username") String username
    );

}
