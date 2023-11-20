package com.team.routineconnect.dto;

import com.team.routineconnect.domain.User;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignUpRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    public User toEntity() {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }
}
