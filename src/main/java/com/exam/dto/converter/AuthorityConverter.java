package com.exam.dto.converter;



import com.exam.config.UserAuthority;
import jakarta.persistence.AttributeConverter;

import java.util.Arrays;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthorityConverter implements AttributeConverter<List<UserAuthority>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<UserAuthority> attribute) {
        return Optional.ofNullable(attribute)
                .map(authorities -> authorities.stream()
                        .map(UserAuthority::getAuthority)
                        .collect(Collectors.joining(DELIMITER))
                ).orElse(null);
    }

    @Override
    public List<UserAuthority> convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
                .map(authorities -> Arrays.stream(authorities.split(DELIMITER))
                        .map(UserAuthority::new)
                        .collect(Collectors.toList()))
                .orElse(null);
    }
}
