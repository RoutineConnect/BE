package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ItemUpdate {
    @NotNull
    private Long itemOrderId;
    @NotNull
    private Double position;
}
