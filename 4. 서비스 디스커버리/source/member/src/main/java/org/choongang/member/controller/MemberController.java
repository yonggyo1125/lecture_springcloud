package org.choongang.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.member.common.exceptions.BadRequestException;
import org.choongang.member.service.JoinService;
import org.choongang.member.service.JoinValidator;
import org.choongang.member.service.client.Board;
import org.choongang.member.service.client.BoardDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final JoinService joinService;
    private final JoinValidator joinValidator;
    private final BoardDiscoveryClient boardDiscoveryClient;

    /**
     * 회원 가입 처리
     * @param form
     * @param errors
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> join(@Valid @RequestBody RequestJoin form, Errors errors) {
        joinValidator.validate(form, errors);

        errorProcess(errors);

        joinService.join(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/board")
    public List<Board> getBoards() {
        return boardDiscoveryClient.getBoards();
    }

    private void errorProcess(Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(errors);
        }
    }
}
