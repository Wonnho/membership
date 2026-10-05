package com.membership.member.Service;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MembershipClosureService {
    private final MemberRepository members;
    private final SessionRegistry sessions;
    public MembershipClosureService(MemberRepository members,SessionRegistry sessions) {
        this.members=members; this.sessions=sessions;
    }
    @Transactional
    public void close(Long id) {
        Member member=members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if("ADMIN".equals(member.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        String email=member.getEmail();
        members.delete(member);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                for(Object principal:sessions.getAllPrincipals()) {
                    if(principal instanceof UserDetails user && user.getUsername().equals(email))
                        sessions.getAllSessions(principal,false).forEach(s -> s.expireNow());
                }
            }
        });
    }
}
