package org.choongang.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.member.constant.Authority;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class Member extends Base {
    @Id
    @GeneratedValue
    private Long seq;

    @Column(length=65, unique = true)
    private String email;

    @Column(length=65)
    private String password;

    @Column(length=65)
    private String confirmPassword;

    @Column(length=45)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable = false)
    private Authority authority = Authority.USER;
}
