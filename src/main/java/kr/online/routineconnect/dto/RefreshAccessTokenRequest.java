package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshAccessTokenRequest(@NotBlank String grantType, @NotBlank String refreshToken) {
}
