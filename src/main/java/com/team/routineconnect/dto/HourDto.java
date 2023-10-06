package com.team.routineconnect.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.repository.HourRepository;

public class HourDto {
    private final Object hour;

    @JsonCreator
    public HourDto(Object hour) {
        this.hour = hour;
    }

    @JsonValue
    public Object getHour() {
        return hour;
    }

    public Hour toEntity(HourRepository hourRepository) {
        Hour hour = null;
        if (this.hour != null) {
            if (this.hour instanceof String) {
                hour = Hour.builder()
                        .hour(this.hour.toString())
                        .build();
            } else if (this.hour instanceof Hour) {
                hour = hourRepository.findById(((Hour) this.hour).getId())
                        .orElseThrow(() -> new IllegalArgumentException("Illegal hour ID"));
            } else {
                throw new IllegalArgumentException("Illegal hourDto type");
            }
        }

        return hour;
    }
}
