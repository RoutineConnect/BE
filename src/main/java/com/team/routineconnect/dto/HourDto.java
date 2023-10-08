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
        if (hour != null) {
            if (hour instanceof String) {
                return Hour.builder()
                        .hour(hour.toString())
                        .build();
            } else if (hour instanceof LinkedHashMap) {
                try {
                    return objectMapper.convertValue(hour, Hour.class);
                } catch (Exception e) {
                    throw e;
                }
            } else {
                throw new IllegalArgumentException("Illegal hourDto type");
            }
        }

        return null;
    }
}
