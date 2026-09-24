package com.membership.member.controller;

import com.membership.member.dto.MemberForm;
import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

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
        return "redirect:/members/"+saved.getId();

    }

  @GetMapping("/members/{id}")
    public String amember(@PathVariable("id") Long id,Model model) {
        //1. find a particular ID
         Member member=memberRepository.findById(id).orElse(null);
        // 2. contains data on model
        model.addAttribute("member",member);
        // 3. view the data on display
        return "membership/amember" ;
  }

    @GetMapping("/members")
    public String allmember(Model model) {
        //1. find all IDs
        List<Member> memberList=memberRepository.findAll();
        // 2. contains data on model
        model.addAttribute("memberList",memberList);
        // 3. view the data on display
        return "membership/allmember" ;

    }

    @GetMapping("/members/update/{id}")
    public  String updateMember(@PathVariable("id") Long id,Model model) {
         Member updateId=memberRepository.findById(id).orElseThrow();
          model.addAttribute("member",updateId);
         return "membership/update";
    }

    @PostMapping("/members/update/{id}")
    public String updatedMember(@PathVariable("id") Long id,MemberForm form) {
        Member member=memberRepository.findById(id).orElseThrow();
            member.update(form.getEmail(),form.getPassword());
                 memberRepository.save(member);
        return "redirect:/members/"+id;
    }


      @PostMapping("/members/delete/{id}")
      public  String delete(@PathVariable("id") Long id) {
       memberRepository.deleteById(id);

         return "redirect:/members";
      }

}
