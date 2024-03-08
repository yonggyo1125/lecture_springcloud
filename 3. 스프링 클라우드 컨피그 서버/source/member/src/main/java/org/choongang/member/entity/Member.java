package org.choongang.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
