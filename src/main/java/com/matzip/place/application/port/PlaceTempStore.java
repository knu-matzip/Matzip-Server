package com.matzip.place.application.port;

import java.util.List;

/**
 * 임시 스냅샷 저장소 Port(추상화)
 * 애플리케이션 계층은 구현체(메모리/Redis 등)에 의존하지 않고 이 인터페이스에만 의존
 */
public interface PlaceTempStore {

    void put(PlaceSnapshot snapshot);

    /**
     * 등록 시점에 사용할 스냅샷을 조회한다.
     * 존재하지 않거나 만료되었으면 null을 반환한다.
     */
    PlaceSnapshot findById(String kakaoPlaceId);

    void remove(String kakaoPlaceId);

    /**
     * 프리뷰 단계에서 확보한 값을 등록 시까지 보존하기 위한 스냅샷 모델
     * 서버가 신뢰하는 값(이름/주소/좌표)을 보존하여, 등록 시 클라이언트 입력을 신뢰하지 않기 위함
     * 사진/메뉴는 정책상 URL/원본 메뉴 기준으로 보존한다(추천 여부는 등록 요청에서 사용자 선택 반영)
     */
    final class PlaceSnapshot {

        private final String kakaoPlaceId;
        private final String placeName;
        private final String address;
        private final double latitude;
        private final double longitude;

        private final List<SMenu> menus;   // 프리뷰 당시 메뉴 스냅샷
        private final List<SPhoto> photos; // 프리뷰 당시 사진 스냅샷

        public PlaceSnapshot(
                String kakaoPlaceId,
                String placeName,
                String address,
                double latitude,
                double longitude,
                List<SMenu> menus,
                List<SPhoto> photos
        ) {
            // 필수 값 검증. 상세 검증은 상위 서비스에서 수행
            if (kakaoPlaceId == null || kakaoPlaceId.isBlank()) {
                throw new IllegalArgumentException("kakaoPlaceId는 비어 있을 수 없습니다.");
            }
            if (placeName == null || placeName.isBlank()) {
                throw new IllegalArgumentException("placeName은 비어 있을 수 없습니다.");
            }
            if (address == null || address.isBlank()) {
                throw new IllegalArgumentException("address는 비어 있을 수 없습니다.");
            }
            this.kakaoPlaceId = kakaoPlaceId;
            this.placeName = placeName;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            // NPE 방지: 불변 리스트 보장(여기서는 null이면 빈 리스트로 대체)
            this.menus = (menus == null) ? List.of() : menus;
            this.photos = (photos == null) ? List.of() : photos;
        }

        public String getKakaoPlaceId() { return kakaoPlaceId; }
        public String getPlaceName() { return placeName; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public List<SMenu> getMenus() { return menus; }
        public List<SPhoto> getPhotos() { return photos; }

        /**
         * 프리뷰 시점의 메뉴 스냅샷(추천 여부는 포함하지 않음)
         * 등록 단계에서 요청의 isRecommended를 이름 매칭으로 반영하기 위함
         */
        public static final class SMenu {
            private final String name;
            private final int price;

            public SMenu(String name, int price) {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("메뉴 이름은 비어 있을 수 없습니다.");
                }
                if (price < 0) {
                    throw new IllegalArgumentException("메뉴 가격은 음수일 수 없습니다.");
                }
                this.name = name;
                this.price = price;
            }

            public String getName() {
                return name;
            }

            public int getPrice() {
                return price;
            }
        }

        /**
         * 프리뷰 시점의 사진 스냅샷
         * 정책상 서버는 파일을 저장하지 않고 외부 URL만 보관한다.
         */
        public static final class SPhoto {
            private final Long photoId;      // 외부에서 식별용으로 제공되면 사용(없으면 null)
            private final String photoUrl;   // 필수
            private final int displayOrder;

            public SPhoto(Long photoId, String photoUrl, int displayOrder) {
                if (photoUrl == null || photoUrl.isBlank()) {
                    throw new IllegalArgumentException("photoUrl은 비어 있을 수 없습니다.");
                }
                this.photoId = photoId;
                this.photoUrl = photoUrl;
                this.displayOrder = displayOrder;
            }

            public Long getPhotoId() { return photoId; }
            public String getPhotoUrl() { return photoUrl; }
            public int getDisplayOrder() { return displayOrder; }
        }
    }
}
