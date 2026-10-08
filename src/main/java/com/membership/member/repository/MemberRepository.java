package com.membership.member.repository;

import com.membership.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /** Finds the member whose unique login email matches the supplied value. */
    Optional<Member> findByEmail(String email);

    /**
     * Finds a member by email and obtains a database write lock for an atomic
     * verification update. This method must be called inside a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.email = :email")
    Optional<Member> lockByEmail(@Param("email") String email);

    /** Finds the member associated with a hashed password-reset token. */
    Optional<Member> findByResetTokenHash(String hash);

    /**
     * Finds a member by reset-token hash and obtains a database write lock so
     * the token can be consumed only once. This method must run in a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.resetTokenHash = :hash")
    Optional<Member> lockByResetTokenHash(@Param("hash") String hash);
}
