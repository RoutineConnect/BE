package com.team.routineconnect.dto;

import com.team.routineconnect.domain.Item;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemResponse {
    private Item item;
    private Float position;
    private Boolean accomplishment;
    private String retrospective;
}
