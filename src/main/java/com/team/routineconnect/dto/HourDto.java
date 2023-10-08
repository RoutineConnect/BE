package com.team.routineconnect.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.domain.Hour;

import java.util.LinkedHashMap;

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

    public Hour toEntity(ObjectMapper objectMapper) {
        Hour hour = null;
        if (this.hour != null) {
            if (this.hour instanceof String) {
                hour = Hour.builder()
                        .hour(this.hour.toString())
                        .build();
            } else if (this.hour instanceof LinkedHashMap) {
                try {
                    hour = objectMapper.convertValue(this.hour, Hour.class);
                } catch (Exception e) {
                    throw e;
                }
            } else {
                throw new IllegalArgumentException("Illegal hourDto type");
            }
        }

        return hour;
    }
}
