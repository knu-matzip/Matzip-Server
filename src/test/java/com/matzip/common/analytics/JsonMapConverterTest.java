package com.matzip.common.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonMapConverterTest {

    private final JsonMapConverter converter = new JsonMapConverter();

    @Test
    void Map를_JSON_문자열로_직렬화하고_다시_Map으로_역직렬화한다() {
        Map<String, Object> properties = Map.of("query", "돈까스", "resultCount", 12);

        String json = converter.convertToDatabaseColumn(properties);
        Map<String, Object> restored = converter.convertToEntityAttribute(json);

        assertThat(json).contains("돈까스");
        assertThat(restored).containsEntry("query", "돈까스");
        assertThat(restored).containsEntry("resultCount", 12);
    }

    @Test
    void null이나_빈_Map은_null로_저장한다() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(Map.of())).isNull();
    }

    @Test
    void null이나_빈_문자열은_null로_복원한다() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }
}
