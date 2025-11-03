package com.matzip.common.config;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.SortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


public class EnumConverters {

    @Component
    public static class StringToCampusConverter implements Converter<String, Campus> {
        @Override
        public Campus convert(String source) {
            return Campus.valueOf(source.toUpperCase());
        }
    }

    @Component
    public static class StringToSortTypeConverter implements Converter<String, SortType> {
        @Override
        public SortType convert(String source) {
            return SortType.valueOf(source.toUpperCase());
        }
    }
}

