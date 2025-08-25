package com.matzip.common.config;

import com.matzip.place.domain.Campus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCampusEnumConverter implements Converter<String, Campus> {

    @Override
    public Campus convert(String source) {
        if (source == null) {
            return null;
        }
        return Campus.valueOf(source.toUpperCase());
    }
}
