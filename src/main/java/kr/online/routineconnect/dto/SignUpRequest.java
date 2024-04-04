package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignUpRequest(@NotBlank String email, @NotBlank String name, @NotBlank String password) {
}
