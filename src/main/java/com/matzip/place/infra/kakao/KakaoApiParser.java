package com.matzip.place.infra.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.matzip.place.dto.MenuDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.common.exception.KakaoApiException;
import com.matzip.common.exception.code.ErrorCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KakaoApiParser {

    /**
     * panel3 응답에서 메뉴 정보를 추출
     * /menu/menus/items 경로에서 메뉴 데이터를 파싱
     */
    public static List<MenuDto> extractMenus(JsonNode root) {
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
     * panel3 응답에서 사진 정보를 추출 (최대 15장까지)
     * 세 가지 출처에서 사진을 수집
     * 1) /menu/menus/photos[*]
     * 2) /photos/photos[*]
     * 3) /blog_review/reviews[*].photos[*]
     */
    public static List<PhotoDto> extractPhotos(JsonNode root) {
        List<PhotoDto> list = new ArrayList<>();
        int order = 0;
        final int MAX_PHOTOS = 15;

        // URL 중복 제거용
        Set<String> dedup = new HashSet<String>();

        // 1) /menu/menus/photos[*]
        JsonNode menus = root.at("/menu/menus");
        if (menus != null && menus.isArray()) {
            for (JsonNode group : menus) {
                if (list.size() >= MAX_PHOTOS) break;
                JsonNode menuPhotos = group.path("photos");
                if (menuPhotos != null && menuPhotos.isArray()) {
                    for (JsonNode p : menuPhotos) {
                        if (list.size() >= MAX_PHOTOS) break;
                        String url = text(p, "photo_url"); // 일부는 url, 일부는 photo_url일 수 있어 둘 다 시도
                        if (url == null) url = text(p, "url");
                        
                        // photoUrl이 null이거나 공백인 경우 제외
                        if (url == null || url.isBlank()) continue;
                        
                        Long pid = numberToLongOrNull(p.get("photo_id"));

                        if (!dedup.contains(url)) {
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
        if (list.size() < MAX_PHOTOS) {
            JsonNode globalPhotos = root.at("/photos/photos");
            if (globalPhotos != null && globalPhotos.isArray()) {
                for (JsonNode p : globalPhotos) {
                    if (list.size() >= MAX_PHOTOS) break;
                    String url = text(p, "photo_url");
                    if (url == null) url = text(p, "url");
                    
                    // photoUrl이 null이거나 공백인 경우 제외
                    if (url == null || url.isBlank()) continue;
                    
                    Long pid = numberToLongOrNull(p.get("photo_id"));

                    if (!dedup.contains(url)) {
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

        // 3) /blog_review/reviews[*].photos[*]
        if (list.size() < MAX_PHOTOS) {
            JsonNode reviews = root.at("/blog_review/reviews");
            if (reviews != null && reviews.isArray()) {
                for (JsonNode r : reviews) {
                    if (list.size() >= MAX_PHOTOS) break;
                    JsonNode reviewPhotos = r.path("photos");
                    if (reviewPhotos != null && reviewPhotos.isArray()) {
                        for (JsonNode p : reviewPhotos) {
                            if (list.size() >= MAX_PHOTOS) break;
                            String url = text(p, "photo_url");
                            if (url == null) url = text(p, "url");
                            
                            // photoUrl이 null이거나 공백인 경우 제외
                            if (url == null || url.isBlank()) continue;
                            
                            Long pid = numberToLongOrNull(p.get("photo_id"));

                            if (!dedup.contains(url)) {
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
        }

        return list;
    }


    /**
     * 문자열이 null이 아니고 공백이 아닌지 확인
     */
    public static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * JsonNode에서 텍스트 필드를 추출
     */
    public static String text(JsonNode root, String field) {
        JsonNode n = (root != null) ? root.get(field) : null;
        return (n != null && n.isTextual()) ? n.asText() : null;
    }

    /**
     * 가격 문자열을 파싱하여 정수로 변환
     * 숫자가 아닌 문자는 제거하고 정수로 변환
     */
    public static int parsePrice(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 주어진 문자열들 중에서 첫 번째로 공백이 아닌 값을 반환
     */
    public static String firstNonBlank(String a, String b, String c) {
        if (isNotBlank(a)) return a;
        if (isNotBlank(b)) return b;
        if (isNotBlank(c)) return c;
        return null;
    }

    /**
     * JsonNode에서 숫자 값을 추출
     */
    public static double requireNumber(JsonNode node, String path) {
        if (node == null || node.isMissingNode() || !node.isNumber()) {
            throw new KakaoApiException(ErrorCode.KAKAO_PANEL3_FIELD_ERROR, 
                    "panel3 필드 누락/형식 오류: " + path);
        }
        return node.asDouble();
    }

    public static Long numberToLongOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.canConvertToLong()) return node.asLong();
        return null;
    }
}
