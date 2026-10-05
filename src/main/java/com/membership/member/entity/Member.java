package com.membership.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    private boolean emailVerified;
    private String verificationHash;
    private java.time.Instant verificationExpiresAt;
    private java.time.Instant verificationSentAt;
    private java.time.Instant verificationWindow;
    private int verificationAttempts;
    private int verificationSends;

    public void issueVerification(String hash, java.time.Instant now) {
        if (verificationWindow==null || !verificationWindow.plusSeconds(3600).isAfter(now)) {
            verificationWindow=now;
            verificationSends=0;
        }
        verificationHash=hash;
        verificationExpiresAt=now.plusSeconds(600);
        verificationSentAt=now;
        verificationAttempts=0;
        verificationSends++;
    }
    public void countVerificationAttempt() { verificationAttempts++; }
    public void confirmEmail() {
        emailVerified=true;
        verificationHash=null;
        verificationExpiresAt=null;
        verificationAttempts=0;
    }

    @Column(nullable = false)
    private String role = "MEMBER";
    private String resetTokenHash;
    private java.time.Instant resetExpiresAt;

    public void makeAdmin() { this.role = "ADMIN"; confirmEmail(); }
    public void issueReset(String hash, java.time.Instant expiry) {
        this.resetTokenHash = hash;
        this.resetExpiresAt = expiry;
    }


    public Member( String email, String password) {
        this.email=email;
        this.password=password;
    }

    public void update(String email, String password) {
        if (!this.email.equals(email)) {
            emailVerified=false;
            verificationHash=null;
            verificationExpiresAt=null;
        }
        this.email=email;
        this.password=password;
        this.resetTokenHash=null;
        this.resetExpiresAt=null;
    }

}
