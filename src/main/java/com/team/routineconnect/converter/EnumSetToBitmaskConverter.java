package com.team.routineconnect.converter;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.DayOfWeek;
import java.util.EnumSet;

@Component
@Configurable
@Converter
public class EnumSetToBitmaskConverter implements AttributeConverter<EnumSet<DayOfWeek>, Byte> {

    @Override
    public Byte convertToDatabaseColumn(EnumSet<DayOfWeek> attribute) {
        byte bitmask = 0;
        for (DayOfWeek dayOfWeek : attribute) {
            bitmask |= 1 << dayOfWeek.getValue();
        }
        return bitmask;
    }

    @Override
    public EnumSet<DayOfWeek> convertToEntityAttribute(Byte dbData) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if ((dbData & (1 << dayOfWeek.getValue())) != 0) {
                days.add(dayOfWeek);
            }
        }
        return days;
    }
}
