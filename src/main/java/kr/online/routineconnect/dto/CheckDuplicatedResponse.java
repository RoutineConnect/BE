package kr.online.routineconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum CheckDuplicatedResponse {
    SUCCESS(false, "사용 가능합니다."),
    ERROR(true, "이미 사용 중 입니다.");

    private final boolean isDuplicated;

    private final String message;

    @JsonProperty("is_duplicated")
    public boolean isDuplicated() {
        return isDuplicated;
    }
}
