package kr.online.routineconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
public enum Response {
    SIGN_UP_SUCCESS(true, "가입에 성공하였습니다."),
    SIGN_IN_FAIL(false, "잘못된 아이디 혹은 비밀번호입니다."),
    SUCCESS(true),
    FAIL(false);

    private final boolean isSuccess;

    @JsonInclude(Include.NON_NULL)
    @Getter
    private String message;

    Response(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    @JsonProperty("is_success")
    public boolean isSuccess() {
        return isSuccess;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }
}