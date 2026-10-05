package com.membership.member.controller;

import com.membership.member.Service.EmailVerificationService;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailVerificationController {
    private final EmailVerificationService verification;
    public EmailVerificationController(EmailVerificationService verification) { this.verification=verification; }
    @GetMapping("/membership/verify")
    public String page(HttpSession session,Model model) {
        if(!verification.isEnabled()) return "redirect:/login";
        model.addAttribute("verificationEmail",session.getAttribute("verificationEmail")==null ? ""
                : session.getAttribute("verificationEmail"));
        if(!model.containsAttribute("verificationMessage")) model.addAttribute("verificationMessage","");
        model.addAttribute("formError","");
        model.addAttribute("hasVerificationMessage",!model.getAttribute("verificationMessage").toString().isBlank());
        model.addAttribute("hasFormError",false);
        return "membership/verify";
    }
    @PostMapping("/membership/verify")
    public String verify(@RequestParam("email") String email,@RequestParam("code") String code,
            HttpSession session,Model model) {
        if(!verification.isEnabled()) return "redirect:/login";
        if(verification.verify(email,code.trim())) {
            session.removeAttribute("verificationEmail");
            return "redirect:/login?verified";
        }
        model.addAttribute("verificationEmail",email);
        model.addAttribute("verificationMessage","");
        model.addAttribute("formError","The code is incorrect, expired, or already used. After 5 attempts, request a new code.");
        model.addAttribute("hasVerificationMessage",false);
        model.addAttribute("hasFormError",true);
        return "membership/verify";
    }
    @PostMapping("/membership/verify/resend")
    public String resend(@RequestParam("email") String email,HttpSession session,RedirectAttributes redirect) {
        if(!verification.isEnabled()) return "redirect:/login";
        String normalized=email.trim().toLowerCase(Locale.ROOT);
        session.setAttribute("verificationEmail",normalized);
        try {
            verification.resend(normalized);
            redirect.addFlashAttribute("verificationMessage",
                    "If this email has a pending account and can receive a new code, one has been sent. Wait 60 seconds between requests (up to 5 per hour).");
        } catch(IllegalStateException ex) {
            redirect.addFlashAttribute("verificationMessage",ex.getMessage());
        }
        return "redirect:/membership/verify";
    }
}
