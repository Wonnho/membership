package com.membership.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SessionActivityController {
    // Reached only after authentication, CSRF and server-side idle checks.
    @PostMapping("/session/activity")
    public ResponseEntity<Void> activity() { return ResponseEntity.noContent().build(); }
}
