package kr.online.routineconnect.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RoutineRequest {

    @NotEmpty
    private String title;
    private String hour;
    @NotNull
    private String routineDay;
    @NotNull
    private Boolean shared;
    @NotNull
    private LocalDate createdDate;
    private LocalDate endedDate;
}
