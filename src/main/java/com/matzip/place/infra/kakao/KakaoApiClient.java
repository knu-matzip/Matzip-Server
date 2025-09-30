package com.matzip.place.infra.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.matzip.place.dto.MenuDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.common.exception.KakaoApiException;
import com.matzip.common.exception.code.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;

@Component
public class KakaoApiClient {

    private static final String BASE_URL = "https://place-api.map.kakao.com";
    private static final String REFERER = "https://place.map.kakao.com/";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private final RestClient restClient;

    public KakaoApiClient() {
        this(RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Pf", "web")
                .defaultHeader(HttpHeaders.REFERER, REFERER)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build());
    }

    public KakaoApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * panel3를 한 번 호출하여 기본 정보(이름/주소/좌표/confirm_id) + 메뉴 + 사진을 모두 수집
     * 프론트는 kakaoPlaceId(String)만 전달
     * 서버는 panel3의 /summary/confirm_id와 정확히 일치하는지 검증
     * 주소는 panel3 기준 우선순위(road → disp → jibun)로 결정
     */
    public PanelSnapshot getPanelSnapshot(String kakaoPlaceId) {
        try {
            JsonNode panel = restClient.get()
                    .uri("/places/panel3/{placeId}", kakaoPlaceId) // String ID 사용
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);


            // 1) 기본 정보 추출
            String confirmId = KakaoApiParser.text(panel.at("/summary"), "confirm_id");
            if (confirmId == null || confirmId.isBlank()) {
                throw new KakaoApiException(ErrorCode.KAKAO_CONFIRM_ID_MISSING, 
                        "panel3 응답에 confirm_id가 없습니다. placeId=" + kakaoPlaceId);
            }
            // 요청 kakaoPlaceId와 응답 confirm_id가 불일치하면 저장/표시를 중단
            if (!kakaoPlaceId.equals(confirmId)) {
                throw new KakaoApiException(ErrorCode.KAKAO_CONFIRM_ID_MISMATCH, 
                        "요청 kakaoPlaceId와 응답 confirm_id가 일치하지 않습니다. req=" +
                        kakaoPlaceId + ", resp=" + confirmId);
            }

            String placeName = KakaoApiParser.text(panel.at("/summary"), "name");

            // 주소 우선순위: road → disp → jibun
            JsonNode addr = panel.at("/summary/address");
            String addressRoad = KakaoApiParser.text(addr, "road");
            String addressDisp  = KakaoApiParser.text(addr, "disp");
            String addressJibun = KakaoApiParser.text(addr, "jibun");
            String finalAddress = KakaoApiParser.firstNonBlank(addressRoad, addressDisp, addressJibun);

            // 좌표: lon/lat 명시 필드 사용
            double latitude  = KakaoApiParser.requireNumber(panel.at("/summary/point/lat"), "summary.point.lat");
            double longitude = KakaoApiParser.requireNumber(panel.at("/summary/point/lon"), "summary.point.lon");

            // 2) 메뉴/사진 추출
            List<MenuDto> menus = KakaoApiParser.extractMenus(panel);
            List<PhotoDto> photos = KakaoApiParser.extractPhotos(panel);

            return new PanelSnapshot(confirmId, placeName, finalAddress, latitude, longitude, menus, photos);
        } catch (RestClientResponseException e) {
            throw new KakaoApiException(ErrorCode.KAKAO_PANEL3_CALL_FAILED, 
                    "Kakao panel3 호출 실패: status=" + e.getStatusCode().value() +
                            ", body=" + e.getResponseBodyAsString(), e);
        }
    }

    /**
     * panel3에서 뽑은 스냅샷
     * 등록 시 클라이언트가 보내는 값은 무시하고 아래 값 사용
     */
    public record PanelSnapshot(
            String confirmId,
            String placeName,
            String address,
            double latitude,
            double longitude,
            List<MenuDto> menus,
            List<PhotoDto> photos
    ) {}

}
