package com.team.routineconnect.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ItemUpdate {
    @NotNull
    private Long item_id;
    @NotNull
    private Float position;
}
