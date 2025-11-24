package com.matzip.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.matzip.common.config.EnumConverters.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToCampusConverter stringToCampusConverter;
    private final StringToSortTypeConverter stringToSortTypeConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToCampusConverter);
        registry.addConverter(stringToSortTypeConverter);
    }
}
