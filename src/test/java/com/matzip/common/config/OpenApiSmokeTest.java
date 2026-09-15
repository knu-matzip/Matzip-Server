package com.matzip.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "springdoc.paths-to-exclude=/api/v1/auth/dev/**")
class OpenApiSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void apiDocs_토큰없이_200_반환하고_경로포함() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("/api/v1/places");
        assertThat(response.getBody()).contains("bearerAuth");
        assertThat(response.getBody()).doesNotContain("/api/v1/auth/dev/token");
    }

    @Test
    void apiDocs_컨트롤러_어노테이션_반영() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // @Tag: 도메인 한글 태그로 그룹화
        assertThat(response.getBody()).contains("\"맛집\"");
        // @Operation: 엔드포인트 요약
        assertThat(response.getBody()).contains("맛집 상세 조회");
    }

    @Test
    void 모든_엔드포인트에_200_응답_존재() throws Exception {
        // 에러 @ApiResponse만 선언하면 springdoc가 기본 200을 대체해 성공 응답이 누락된다.
        // 모든 오퍼레이션에 200이 남아있는지 회귀 방지로 검증한다.
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode paths = new ObjectMapper().readTree(response.getBody()).get("paths");
        paths.fields().forEachRemaining(pathEntry ->
                pathEntry.getValue().fields().forEachRemaining(opEntry -> {
                    JsonNode responses = opEntry.getValue().get("responses");
                    assertThat(responses.has("200"))
                            .as("%s %s 에 200 응답이 있어야 한다", opEntry.getKey(), pathEntry.getKey())
                            .isTrue();
                }));
    }

    @Test
    void swaggerUi_토큰없이_401아님() {
        ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui/index.html", String.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
