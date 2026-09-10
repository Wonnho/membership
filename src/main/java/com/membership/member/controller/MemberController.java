package com.membership.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MemberController {

    @GetMapping("/adios")
    //@ResponseBody
    public String adios(Model model) {
        model.addAttribute("amigos","Amigos");
        return "front";
    }

    @PostMapping("/join")
    public String join() {

        return "/newmember";

    }
}
