package com.membership.member.controller;

import com.membership.member.Service.PasswordResetService;
import com.membership.member.repository.MemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordResetController {
    private final PasswordResetService resets;
    private final MemberRepository members;
    public PasswordResetController(PasswordResetService resets, MemberRepository members) {
        this.resets = resets; this.members = members;
    }
    @PostMapping("/members/{id}/reset")
    public String issue(@PathVariable("id") Long id, Model model) {
        String token = resets.issue(id);
        model.addAttribute("resetPath", "/password/reset?token=" + token);
        model.addAttribute("recipient", members.findById(id).orElseThrow().getEmail());
        return "membership/reset-issued";
    }
    @GetMapping("/password/reset")
    public String form(@RequestParam(name="token", defaultValue="") String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("validLink", resets.valid(token));
        model.addAttribute("formError", "");
        return "membership/reset";
    }
    @PostMapping("/password/reset")
    public String reset(@RequestParam("token") String token, @RequestParam("password") String password,
            @RequestParam("confirmation") String confirmation, Model model) {
        try {
            if (!password.equals(confirmation)) throw new IllegalArgumentException("Passwords do not match.");
            resets.reset(token, password);
            return "redirect:/login?updated";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("token", token);
            model.addAttribute("validLink", resets.valid(token));
            model.addAttribute("formError", ex.getMessage());
            return "membership/reset";
        }
    }
}
