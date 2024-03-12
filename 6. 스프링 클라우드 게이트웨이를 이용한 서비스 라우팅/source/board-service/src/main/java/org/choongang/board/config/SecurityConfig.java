package org.choongang.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // 이 클래스는 @Configuration이 설정되어야 한다.
@EnableWebSecurity // 전역 WebSecurity 구성을 적용한다.
@EnableMethodSecurity(jsr250Enabled = true) // @RoleAllowed를 활성화한다.
public class SecurityConfig  {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable());

        http.authorizeHttpRequests(c -> c.anyRequest().authenticated());


        return http.build();
    }
}
