package com.membership.member.repository;

import com.membership.member.entity.Member;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface MemberRepository extends CrudRepository<Member,Long> {


    @Override
    ArrayList<Member> findAll();
}
