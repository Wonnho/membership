package com.membership.member.dto;

import com.membership.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@ToString
public class MemberForm {

    private String email;

    private String password;

    public Member toEntity() {
      return new Member(email,password);
    }
}
