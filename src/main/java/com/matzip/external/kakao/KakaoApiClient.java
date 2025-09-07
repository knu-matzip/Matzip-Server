package com.matzip.external.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.matzip.common.dto.MenuDto;
import com.matzip.common.dto.PhotoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * 프론트는 kakaoPlaceId(String)만 전달.
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
            String confirmId = text(panel.at("/summary"), "confirm_id");
            if (confirmId == null || confirmId.isBlank()) {
                throw new KakaoApiException("panel3 응답에 confirm_id가 없습니다. placeId=" + kakaoPlaceId, null);
            }
            // 요청 kakaoPlaceId와 응답 confirm_id가 불일치하면 저장/표시를 중단
            if (!kakaoPlaceId.equals(confirmId)) {
                throw new KakaoApiException("요청 kakaoPlaceId와 응답 confirm_id가 일치하지 않습니다. req=" +
                        kakaoPlaceId + ", resp=" + confirmId, null);
            }

            String placeName = text(panel.at("/summary"), "name");

            // 주소 우선순위: road → disp → jibun
            JsonNode addr = panel.at("/summary/address");
            String addressRoad = text(addr, "road");
            String addressDisp  = text(addr, "disp");
            String addressJibun = text(addr, "jibun");
            String finalAddress = firstNonBlank(addressRoad, addressDisp, addressJibun);

            // 좌표: lon/lat 명시 필드 사용
            double latitude  = requireNumber(panel.at("/summary/point/lat"), "summary.point.lat");
            double longitude = requireNumber(panel.at("/summary/point/lon"), "summary.point.lon");

            // 2) 메뉴/사진 추출
            List<MenuDto> menus = extractMenus(panel);
            List<PhotoDto> photos = extractPhotos(panel);

            return new PanelSnapshot(confirmId, placeName, finalAddress, latitude, longitude, menus, photos);
        } catch (RestClientResponseException e) {
            throw new KakaoApiException(
                    "Kakao panel3 호출 실패: status=" + e.getStatusCode().value() +
                            ", body=" + e.getResponseBodyAsString(), e);
        }
    }


    // ======= 추출 로직 =======

    // /menu/menus/items
    private static List<MenuDto> extractMenus(JsonNode root) {
        List<MenuDto> list = new ArrayList<>();

        JsonNode items = root.at("/menu/menus/items");
        if (items != null && items.isArray()) {
            for (JsonNode item : items) {

                JsonNode idNode = item.get("product_id");
                if (idNode == null || !idNode.canConvertToLong()) {
                    continue; // 식별 불가 항목 스킵
                }

                long productId = idNode.asLong();
                if (productId <= 0L) {
                    continue;
                }

                String name = text(item, "name");
                if (name == null || name.isBlank()) continue;

                // Kakao: price가 -1이면 "미표기"로 간주 -> 0으로 정규화
                int price = 0;
                JsonNode priceNode = item.get("price");
                if (priceNode != null && priceNode.isInt()) {
                    int raw = priceNode.asInt();
                    price = (raw >= 0) ? raw : 0;
                } else {
                    String priceText = text(item, "price");
                    price = parsePrice(priceText);
                }

                list.add(MenuDto.builder()
                        .menuId(productId)
                        .name(name)
                        .price(price)
                        .build());
            }
        }
        return list;
    }

    /**
     * 사진은 세 출처 합산
     */
    private static List<PhotoDto> extractPhotos(JsonNode root) {
        List<PhotoDto> list = new ArrayList<>();
        int order = 0;

        // URL 중복 제거용
        Set<String> dedup = new HashSet<String>();

        // 1) /menu/menus/photos[*]
        JsonNode menus = root.at("/menu/menus");
        if (menus != null && menus.isArray()) {
            for (JsonNode group : menus) {
                JsonNode menuPhotos = group.path("photos");
                if (menuPhotos != null && menuPhotos.isArray()) {
                    for (JsonNode p : menuPhotos) {
                        String url = text(p, "photo_url"); // 일부는 url, 일부는 photo_url일 수 있어 둘 다 시도
                        if (url == null) url = text(p, "url");
                        Long pid = numberToLongOrNull(p.get("photo_id"));

                        if (isNotBlank(url) && !dedup.contains(url)) {
                            list.add(PhotoDto.builder()
                                    .photoId(pid)             // 있으면 세팅, 없으면 null
                                    .photoUrl(url)
                                    .displayOrder(order++)
                                    .build());
                            dedup.add(url);
                        }
                    }
                }
            }
        }

        // 2) /photos/photos[*]
        JsonNode globalPhotos = root.at("/photos/photos");
        if (globalPhotos != null && globalPhotos.isArray()) {
            for (JsonNode p : globalPhotos) {
                String url = text(p, "photo_url");
                if (url == null) url = text(p, "url");
                Long pid = numberToLongOrNull(p.get("photo_id"));

                if (isNotBlank(url) && !dedup.contains(url)) {
                    list.add(PhotoDto.builder()
                            .photoId(pid)
                            .photoUrl(url)
                            .displayOrder(order++)
                            .build());
                    dedup.add(url);
                }
            }
        }

        // 3) /blog_review/reviews[*].photos[*]
        JsonNode reviews = root.at("/blog_review/reviews");
        if (reviews != null && reviews.isArray()) {
            for (JsonNode r : reviews) {
                JsonNode reviewPhotos = r.path("photos");
                if (reviewPhotos != null && reviewPhotos.isArray()) {
                    for (JsonNode p : reviewPhotos) {
                        String url = text(p, "photo_url");
                        if (url == null) url = text(p, "url");
                        Long pid = numberToLongOrNull(p.get("photo_id"));

                        if (isNotBlank(url) && !dedup.contains(url)) {
                            list.add(PhotoDto.builder()
                                    .photoId(pid)
                                    .photoUrl(url)
                                    .displayOrder(order++)
                                    .build());
                            dedup.add(url);
                        }
                    }
                }
            }
        }

        return list;
    }

    // ======= 유틸 =======

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String text(JsonNode root, String field) {
        JsonNode n = (root != null) ? root.get(field) : null;
        return (n != null && n.isTextual()) ? n.asText() : null;
    }

    private static int parsePrice(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0d;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0d;
        }
    }

    private static String firstNonBlank(String a, String b, String c) {
        if (isNotBlank(a)) return a;
        if (isNotBlank(b)) return b;
        if (isNotBlank(c)) return c;
        return null;
    }

    private static double requireNumber(JsonNode node, String path) {
        if (node == null || node.isMissingNode() || !node.isNumber()) {
            throw new KakaoApiException("panel3 필드 누락/형식 오류: " + path, null);
        }
        return node.asDouble();
    }

    private static Long numberToLongOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.canConvertToLong()) return node.asLong();
        return null;
    }

    /**
     * panel3에서 뽑은 스냅샷(서버 신뢰 소스).
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

    public static class KakaoApiException extends RuntimeException {
        public KakaoApiException(String message, Throwable cause) { super(message, cause); }
    }
}
