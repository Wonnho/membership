package com.membership.member.dto;

import com.membership.member.entity.Member;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class MemberForm {

    private String email;

    private String password;

    public Member toEntity() {
      return new Member(email,password);
    }
}
