package com.membership.member.repository;

import com.membership.member.entity.Member;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface MemberRepository extends CrudRepository<Member,Long> {
    java.util.Optional<Member> findByEmail(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Member m where m.email = :email")
    java.util.Optional<Member> lockByEmail(@org.springframework.data.repository.query.Param("email") String email);
    java.util.Optional<Member> findByResetTokenHash(String hash);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Member m where m.resetTokenHash = :hash")
    java.util.Optional<Member> lockByResetTokenHash(@org.springframework.data.repository.query.Param("hash") String hash);


    @Override
    ArrayList<Member> findAll();
}
