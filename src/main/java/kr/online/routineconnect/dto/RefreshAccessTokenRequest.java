package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenRequest(@NotBlank String refreshToken) {
}
