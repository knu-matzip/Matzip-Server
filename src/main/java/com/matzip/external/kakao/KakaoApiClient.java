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

    // 메뉴, 사진만 추출해서 반환
    public MenusAndPhotos getMenusAndPhotos(long placeId) {
        try {
            JsonNode panel = restClient.get()
                    .uri("/places/panel3/{placeId}", placeId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            return new MenusAndPhotos(extractMenus(panel), extractPhotos(panel));
        } catch (RestClientResponseException e) {
            throw new KakaoApiException(
                    "Kakao panel3 호출 실패: status=" + e.getStatusCode().value() +
                            ", body=" + e.getResponseBodyAsString(), e);
        }
    }

    // 실제 응답 구조에 맞춘 메뉴 추출: /menu/menus/items
    private static List<MenuDto> extractMenus(JsonNode root) {
        List<MenuDto> list = new ArrayList<>();
        JsonNode items = root.at("/menu/menus/items");
        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                String name = text(item, "name");
                if (name == null || name.isBlank()) continue;

                // price는 정수로 내려오며, -1은 '가격 미표기' 의미로 간주
                int price = 0;
                JsonNode priceNode = item.get("price");
                if (priceNode != null && priceNode.isInt()) {
                    int raw = priceNode.asInt();
                    price = (raw >= 0) ? raw : 0;
                } else {
                    // 혹시 문자열로 내려오는 경우 방어로직
                    String priceText = text(item, "price");
                    price = parsePrice(priceText);
                }

                list.add(MenuDto.builder()
                        .name(name)
                        .price(price)
                        .build());
            }
        }
        return list;
    }

    /**
     * 실제 응답 구조에 맞춘 사진 추출 (합산):
     * 1) /menu/menus/photos[*].url
     * 2) /photos/photos[*].url
     * 3) /blog_review/reviews[*].photos[*].url
     */
    private static List<PhotoDto> extractPhotos(JsonNode root) {
        List<PhotoDto> list = new ArrayList<>();
        int order = 0;

        // 1) menu.menus.photos
        JsonNode menuPhotos = root.at("/menu/menus/photos");
        if (menuPhotos != null && menuPhotos.isArray()) {
            for (JsonNode p : menuPhotos) {
                String url = text(p, "url");
                if (isNotBlank(url)) {
                    list.add(PhotoDto.builder()
                            .photoId(null)
                            .photoUrl(url)
                            .displayOrder(order++)
                            .build());
                }
            }
        }

        // 2) photos.photos
        JsonNode globalPhotos = root.at("/photos/photos");
        if (globalPhotos != null && globalPhotos.isArray()) {
            for (JsonNode p : globalPhotos) {
                String url = text(p, "url");
                if (isNotBlank(url)) {
                    list.add(PhotoDto.builder()
                            .photoId(null)
                            .photoUrl(url)
                            .displayOrder(order++)
                            .build());
                }
            }
        }

        // 3) blog_review.reviews[*].photos[*]
        JsonNode reviews = root.at("/blog_review/reviews");
        if (reviews != null && reviews.isArray()) {
            for (JsonNode r : reviews) {
                JsonNode reviewPhotos = r.path("photos");
                if (reviewPhotos != null && reviewPhotos.isArray()) {
                    for (JsonNode p : reviewPhotos) {
                        String url = text(p, "url");
                        if (isNotBlank(url)) {
                            list.add(PhotoDto.builder()
                                    .photoId(null)
                                    .photoUrl(url)
                                    .displayOrder(order++)
                                    .build());
                        }
                    }
                }
            }
        }

        return list;
    }

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

    public record MenusAndPhotos(List<MenuDto> menus, List<PhotoDto> photos) {}

    public static class KakaoApiException extends RuntimeException {
        public KakaoApiException(String message, Throwable cause) { super(message, cause); }
    }
}
