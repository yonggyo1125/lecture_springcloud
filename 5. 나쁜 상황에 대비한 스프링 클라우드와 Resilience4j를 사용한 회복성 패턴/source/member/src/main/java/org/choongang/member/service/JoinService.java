package org.choongang.member.service;

import lombok.RequiredArgsConstructor;
import org.choongang.member.controller.RequestJoin;
import org.choongang.member.entity.Member;
import org.choongang.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    public void join(RequestJoin form) {
        String hash = encoder.encode(form.getPassword());
        Member member = Member.builder()
                .email(form.getEmail())
                .password(hash)
                .name(form.getName())
                .build();

        memberRepository.saveAndFlush(member);
    }
}