package kr.online.routineconnect.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Converter
public class AuthoritiesToStringConverter implements
        AttributeConverter<Collection<? extends GrantedAuthority>, String> {

    @Override
    public String convertToDatabaseColumn(Collection<? extends GrantedAuthority> attribute) {
        return attribute.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    @Override
    public Collection<? extends GrantedAuthority> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
