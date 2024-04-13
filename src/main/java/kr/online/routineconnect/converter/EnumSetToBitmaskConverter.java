package kr.online.routineconnect.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;
import java.util.EnumSet;
import org.springframework.stereotype.Component;

@Component
@Converter
public class EnumSetToBitmaskConverter implements AttributeConverter<EnumSet<DayOfWeek>, Byte> {

    @Override
    public Byte convertToDatabaseColumn(EnumSet<DayOfWeek> attribute) {
        byte bitmask = 0;
        for (DayOfWeek dayOfWeek : attribute) {
            bitmask |= (byte) (1 << dayOfWeek.ordinal());
        }
        return bitmask;
    }

    @Override
    public EnumSet<DayOfWeek> convertToEntityAttribute(Byte dbData) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if ((dbData & (1 << dayOfWeek.ordinal())) != 0) {
                days.add(dayOfWeek);
            }
        }
        return days;
    }
}
