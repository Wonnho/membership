package com.membership.member.Service;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {
    private final MemberRepository members;
    private final PasswordEncoder passwords;
    public PasswordResetService(MemberRepository members, PasswordEncoder passwords) {
        this.members = members; this.passwords = passwords;
    }
    @Transactional
    public String issue(Long id) {
        Member member = members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (member.getRole().equals("ADMIN")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        member.issueReset(hash(token), Instant.now().plusSeconds(1800));
        return token;
    }
    public boolean valid(String token) {
        return members.findByResetTokenHash(hash(token))
                .filter(m -> m.getResetExpiresAt() != null && m.getResetExpiresAt().isAfter(Instant.now())).isPresent();
    }
    @Transactional
    public void reset(String token, String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use at least 8 characters and no more than 72 UTF-8 bytes.");
        }
        Member member = members.lockByResetTokenHash(hash(token))
                .filter(m -> m.getResetExpiresAt() != null && m.getResetExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("This reset link has expired or has already been used."));
        member.update(member.getEmail(), passwords.encode(password));
    }
    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((token == null ? "" : token).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
