package com.membership.member.controller;

import com.membership.member.dto.MemberForm;
import com.membership.member.enitty.Member;
import com.membership.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/adios")
    //@ResponseBody
    public String adios(Model model) {
        model.addAttribute("amigos","Amigos");
        return "front";
    }

    @GetMapping("/membership/signup")
    public String signup() {

        return "/membership/signup";

    }

    @PostMapping("/join")
    public String joinMember(MemberForm form) {
        Member member=form.toEntity();
        log.info(member.toString());
        Member saved=memberRepository.save(member);
      // System.out.println(saved.toString());
        log.info(saved.toString());
        return "";

    }



}
