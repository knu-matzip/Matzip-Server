package com.matzip.place.client;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 임시 스냅샷 저장소
 * Todo 운영 환경에선 Redis??
 */
@Component
public class PlaceTempStoreMemory {

    private static final Duration TTL = Duration.ofMinutes(10);

    // key: kakaoPlaceId
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void put(PlaceSnapshot snapshot) {
        if (snapshot == null || snapshot.getKakaoPlaceId() == null || snapshot.getKakaoPlaceId().isBlank()) {
            throw new IllegalArgumentException("snapshot 또는 kakaoPlaceId가 유효하지 않습니다.");
        }
        cleanupExpired();
        Instant expireAt = Instant.now().plus(TTL);
        store.put(snapshot.getKakaoPlaceId(), new Entry(snapshot, expireAt));
    }

    public PlaceSnapshot findById(String kakaoPlaceId) {
        if (kakaoPlaceId == null || kakaoPlaceId.isBlank()) {
            return null;
        }
        cleanupExpired();
        Entry e = store.get(kakaoPlaceId);
        if (e == null) {
            return null;
        }
        if (Instant.now().isAfter(e.expireAt)) {
            store.remove(kakaoPlaceId);
            return null;
        }
        return e.snapshot;
    }

    public void remove(String kakaoPlaceId) {
        if (kakaoPlaceId != null) {
            store.remove(kakaoPlaceId);
        }
    }

    /**
     * 만료된 엔트리를 제거
     */
    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Entry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> cur = it.next();
            Entry val = cur.getValue();
            if (now.isAfter(val.expireAt)) {
                it.remove();
            }
        }
    }

    private static final class Entry {
        private final PlaceSnapshot snapshot;
        private final Instant expireAt;

        private Entry(PlaceSnapshot snapshot, Instant expireAt) {
            this.snapshot = snapshot;
            this.expireAt = expireAt;
        }
    }
}
