package org.choongang.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.choongang.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[통합]회원가입 테스트")
    void joinTest() throws Exception {
        RequestJoin form = new RequestJoin();
        form.setEmail("user01@test.org");
        form.setName("사용자01");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setAgree(true);
        String params = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/api/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(params)).andDo(print())
                .andExpect(status().isCreated());

        assertTrue(memberRepository.exists(form.getEmail()));
    }
}
