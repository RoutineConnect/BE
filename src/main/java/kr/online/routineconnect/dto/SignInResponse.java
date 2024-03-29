package kr.online.routineconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.online.routineconnect.util.TokenUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@Builder
@RequiredArgsConstructor
public class SignInResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String type = TokenUtil.TYPE;
}
