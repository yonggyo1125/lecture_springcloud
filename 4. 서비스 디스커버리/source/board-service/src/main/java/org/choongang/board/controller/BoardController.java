package org.choongang.board.controller;

import org.choongang.board.entity.Board;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/board")
public class BoardController {

    @GetMapping
    public List<Board> list() {
        List<Board> items = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Board.builder()
                        .bid("board" + i)
                        .bName("게시판" + i)
                        .build())
                .toList();

        return items;
    }
}
