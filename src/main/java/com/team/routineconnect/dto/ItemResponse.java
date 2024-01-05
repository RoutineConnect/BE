package com.team.routineconnect.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemResponse {
    private String hour;
    private String title;
    private Double position;
    private Boolean accomplishment;
    private String retrospective;
}
