package org.choongang.member.service;

import lombok.RequiredArgsConstructor;
import org.choongang.member.controller.RequestJoin;
import org.choongang.member.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class JoinValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestJoin.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestJoin form = (RequestJoin)target;
        String email = form.getEmail();
        String password = form.getPassword();
        String confirmPassword = form.getConfirmPassword();

        if (StringUtils.hasText(email) && memberRepository.exists(email)) {
            errors.rejectValue("email", "Duplicated");
        }

        if (StringUtils.hasText(password) && StringUtils.hasText(confirmPassword) && !password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", "Duplicated");
        }
    }
}
