package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignInRequest(@NotBlank String email, @NotBlank String password) {
}
