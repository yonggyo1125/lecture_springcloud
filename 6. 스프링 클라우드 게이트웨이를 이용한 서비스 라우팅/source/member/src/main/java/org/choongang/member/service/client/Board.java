package org.choongang.member.service.client;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Board {
    private String bid; // 게시판 아이디

    private String bName; // 게시판명
    private boolean active; // 사용 여부

    private String category; // 게시판 분류

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
