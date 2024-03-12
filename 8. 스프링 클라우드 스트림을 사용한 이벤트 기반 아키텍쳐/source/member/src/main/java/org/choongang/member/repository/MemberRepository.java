package org.choongang.member.repository;

import org.choongang.member.entity.Member;
import org.choongang.member.entity.QMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {
    default boolean exists(String email) {
        return exists(QMember.member.email.eq(email));
    }

    Optional<Member> findByEmail(String email);
}