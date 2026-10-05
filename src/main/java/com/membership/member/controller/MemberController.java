package com.membership.member.controller;

import com.membership.member.config.AdminAccount;
import com.membership.member.dto.MemberForm;
import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import jakarta.servlet.http.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MemberController {
    @ModelAttribute("verificationEnabled")
    public boolean verificationEnabled() { return verification.isEnabled(); }
    private final MemberRepository members;
    private final PasswordEncoder passwords;
    private final com.membership.member.Service.EmailVerificationService verification;
    private final com.membership.member.Service.MembershipClosureService closure;
    public MemberController(MemberRepository members, PasswordEncoder passwords,
            com.membership.member.Service.EmailVerificationService verification,
            com.membership.member.Service.MembershipClosureService closure) {
        this.members=members; this.passwords=passwords; this.verification=verification; this.closure=closure;
    }
    @GetMapping("/adios")
    public String adios(Model model) { model.addAttribute("amigos","Amigos"); return "front"; }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        model.addAttribute("loginError",request.getParameterMap().containsKey("error"));
        model.addAttribute("loggedOut",request.getParameterMap().containsKey("logout"));
        model.addAttribute("registered",request.getParameterMap().containsKey("registered"));
        model.addAttribute("updated",request.getParameterMap().containsKey("updated"));
        model.addAttribute("verified",request.getParameterMap().containsKey("verified"));
        model.addAttribute("membershipClosed",request.getParameterMap().containsKey("closed"));
        model.addAttribute("idleTimeout",request.getParameterMap().containsKey("timeout"));
        model.addAttribute("clearCredentials",request.getParameterMap().containsKey("registered")
                || request.getParameterMap().containsKey("logout") || request.getParameterMap().containsKey("updated")
                || request.getParameterMap().containsKey("closed") || request.getParameterMap().containsKey("timeout"));
        if (!model.containsAttribute("welcomeName")) model.addAttribute("welcomeName","");
        return "membership/login";
    }
    @GetMapping("/membership/signup")
    public String signup(Model model) {
        model.addAttribute("email",""); model.addAttribute("formError","");
        return "membership/signup";
    }
    @PostMapping("/join")
    public String joinMember(MemberForm form, Model model, RedirectAttributes redirect, HttpSession session) {
        String email=normalizeEmail(form.getEmail());
        String error=validate(email,form.getPassword(),false);
        if(error==null && (AdminAccount.EMAIL.equals(email) || members.findByEmail(email).isPresent()))
            error="This email is already registered. Please log in.";
        if(error==null) {
            try {
                verification.register(email,form.getPassword());
                if(!verification.isEnabled()) return "redirect:/login?registered";
                session.setAttribute("verificationEmail",email);
                redirect.addFlashAttribute("verificationMessage","Halo! We sent a code to your email. Enter it below before logging in.");
                return "redirect:/membership/verify";
            } catch (DataIntegrityViolationException ex) { error="This email is already registered. Please log in."; }
              catch (IllegalStateException ex) { error=ex.getMessage(); }
        }
        model.addAttribute("email",email); model.addAttribute("formError",error);
        return "membership/signup";
    }
    @GetMapping("/account")
    public String account(Principal principal) {
        return "redirect:/members/"+members.findByEmail(principal.getName()).orElseThrow().getId();
    }
    @GetMapping("/members")
    public String allmember(Model model) {
        model.addAttribute("memberList",members.findAll());
        return "membership/allmember";
    }
    @GetMapping("/members/{id}")
    public String amember(@PathVariable("id") Long id, Authentication auth, Model model) {
        Member member=accessibleMember(id,auth);
        model.addAttribute("member",member);
        model.addAttribute("canEdit",member.getEmail().equals(auth.getName()));
        model.addAttribute("canWithdraw",member.getEmail().equals(auth.getName()) && !member.getRole().equals("ADMIN"));
        model.addAttribute("canDelete",isAdmin(auth) && !member.getRole().equals("ADMIN"));
        model.addAttribute("canReset",isAdmin(auth) && !member.getRole().equals("ADMIN"));
        return "membership/amember";
    }
    @GetMapping("/members/update/{id}")
    public String updateMember(@PathVariable("id") Long id, Authentication auth, Model model) {
        model.addAttribute("member",ownMember(id,auth)); model.addAttribute("formError","");
        return "membership/update";
    }
    @PostMapping("/members/update/{id}")
    public String updatedMember(@PathVariable("id") Long id, MemberForm form, Authentication auth,
            Model model, HttpServletRequest request, HttpServletResponse response) {
        Member member=ownMember(id,auth);
        String email=normalizeEmail(form.getEmail());
        String error=validate(email,form.getPassword(),true);
        if (member.getRole().equals("ADMIN") && !email.equals(AdminAccount.EMAIL))
            error="The administrator email is fixed.";
        if (error==null && !member.getRole().equals("ADMIN") && AdminAccount.EMAIL.equals(email))
            error="This email is reserved for the administrator.";
        if(error==null && members.findByEmail(email).filter(other -> !other.getId().equals(id)).isPresent())
            error="This email is already registered.";
        if(error==null) {
            String hash=form.getPassword()==null || form.getPassword().isEmpty()
                    ? member.getPassword() : passwords.encode(form.getPassword());
            boolean emailChanged=!member.getEmail().equals(email);
            try {
                verification.updateAccount(id,email,hash);
                new SecurityContextLogoutHandler().logout(request,response,auth);
                if(emailChanged && verification.isEnabled()) {
                    request.getSession(true).setAttribute("verificationEmail",email);
                    return "redirect:/membership/verify";
                }
                return "redirect:/login?updated";
            } catch(DataIntegrityViolationException ex) { error="This email is already registered."; }
              catch(IllegalStateException ex) { error=ex.getMessage(); }
        }
        model.addAttribute("member",member); model.addAttribute("formError",error);
        return "membership/update";
    }
    @PostMapping("/members/delete/{id}")
    public String delete(@PathVariable("id") Long id, Authentication auth,
            HttpServletRequest request,HttpServletResponse response) {
        if (!isAdmin(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Member member=accessibleMember(id,auth);
        if (member.getRole().equals("ADMIN")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        boolean self=member.getEmail().equals(auth.getName());
        closure.close(member.getId());
        if (self) {
            new SecurityContextLogoutHandler().logout(request,response,auth);
            return "redirect:/login?logout";
        }
        return "redirect:/members";
    }
    private boolean isAdmin(Authentication auth) {
        return auth!=null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    @PostMapping("/account/withdraw")
    public String withdraw(Authentication auth,@RequestParam("confirmClosure") boolean confirmed,
            HttpServletRequest request,HttpServletResponse response) {
        if(!confirmed) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Member member=members.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        closure.close(member.getId());
        new SecurityContextLogoutHandler().logout(request,response,auth);
        return "redirect:/login?closed";
    }
    private Member accessibleMember(Long id,Authentication auth) {
        Member member=members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(auth==null || !isAdmin(auth) && !member.getEmail().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return member;
    }
    private Member ownMember(Long id, Authentication auth) {
        Member member=accessibleMember(id,auth);
        if(!member.getEmail().equals(auth.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return member;
    }
    private String normalizeEmail(String email) {
        return email==null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
    private String validate(String email,String password,boolean allowEmpty) {
        if(email.length()>254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return "Enter a valid email address.";
        if(allowEmpty && (password==null || password.isEmpty())) return null;
        if(password==null || password.length()<8 || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length>72)
            return "Use a password of at least 8 characters and at most 72 UTF-8 bytes.";
        return null;
    }
}
