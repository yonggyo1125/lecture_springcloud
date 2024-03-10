package org.choongang.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class Board extends Base {
    @Id
    @Column(length=40)
    private String bid; // 게시판 아이디

    @Column(length=90, nullable = false)
    private String bName; // 게시판명
    private boolean active; // 사용 여부

    @Lob
    private String category; // 게시판 분류
}
