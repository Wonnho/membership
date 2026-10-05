package com.membership.member.Service;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {
    @org.springframework.beans.factory.annotation.Value("${coffee.email-verification.enabled:true}")
    private boolean enabled;
    public boolean isEnabled() { return enabled; }
    private final MemberRepository members;
    private final PasswordEncoder passwords;
    private final VerificationMailer mailer;
    private final SecureRandom random=new SecureRandom();
    public EmailVerificationService(MemberRepository members,PasswordEncoder passwords,VerificationMailer mailer) {
        this.members=members; this.passwords=passwords; this.mailer=mailer;
    }
    @Transactional
    public void register(String email,String password) {
        Member member=members.save(new Member(email,passwords.encode(password)));
        if(enabled) send(member);
    }
    @Transactional
    public void updateAccount(Long id,String email,String hash) {
        Member member=members.findById(id).orElseThrow();
        boolean changed=!member.getEmail().equals(email);
        member.update(email,hash);
        members.save(member);
        if(changed && enabled) send(member);
    }
    @Transactional
    public void resend(String email) {
        if(!enabled) return;
        members.lockByEmail(normalize(email)).filter(m -> !m.isEmailVerified()).ifPresent(member -> {
            Instant now=Instant.now();
            if(member.getVerificationSentAt()!=null && member.getVerificationSentAt().plusSeconds(60).isAfter(now))
                return;
            if(member.getVerificationWindow()!=null && member.getVerificationWindow().plusSeconds(3600).isAfter(now)
                    && member.getVerificationSends()>=5) return;
            send(member);
        });
    }
    @Transactional
    public boolean verify(String email,String code) {
        if(!enabled) return false;
        Member member=members.lockByEmail(normalize(email)).orElse(null);
        if(member==null || member.isEmailVerified() || member.getVerificationHash()==null
                || member.getVerificationExpiresAt()==null
                || !member.getVerificationExpiresAt().isAfter(Instant.now())
                || member.getVerificationAttempts()>=5) return false;
        // Return rather than throw: failed-attempt counts must be committed.
        member.countVerificationAttempt();
        if(code==null || !code.matches("[0-9]{6}") || !passwords.matches(code,member.getVerificationHash()))
            return false;
        member.confirmEmail();
        return true;
    }
    private void send(Member member) {
        String code=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
        member.issueVerification(passwords.encode(code),Instant.now());
        mailer.sendCode(member.getEmail(),code);
    }
    private String normalize(String email) {
        return email==null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
