SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM daily_view_count;
DELETE FROM photo;
DELETE FROM menu;
DELETE FROM place_category;
DELETE FROM place_tag;
DELETE FROM place;
DELETE FROM category;
DELETE FROM tag;

SET FOREIGN_KEY_CHECKS = 1;

select * from place;


INSERT INTO category (id, category_name, icon_key, created_at, updated_at)
VALUES
    (1, '한식', 'korean', NOW(), NOW()),
    (2, '중식', 'chinese', NOW(), NOW()),
    (3, '일식', 'japanese', NOW(), NOW());

INSERT INTO tag (id, tag_name, icon_key, created_at, updated_at)
VALUES
    (1, '가성비', 'value', NOW(), NOW()),
    (2, '혼밥', 'solo', NOW(), NOW()),
    (3, '웨이팅', 'waiting', NOW(), NOW());

-- 3. 맛집 데이터 1,000개 생성
INSERT INTO place (
    id, campus, kakao_place_id, place_name, address,
    latitude, longitude, description, like_count,
    view_count, registered_by, status, created_at, updated_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    n, 'CHEONAN', CONCAT('loadtest-kakao-', LPAD(n, 4, '0')),
    CONCAT('부하테스트 맛집 ', n), CONCAT('충남 천안시 테스트로 ', n),
    36.800000 + (n * 0.0001), 127.100000 + (n * 0.0001),
    CONCAT('k6 상세 조회 응답 속도 측정을 위한 테스트 데이터 #', n),
    MOD(n, 200), MOD(n, 500), NULL, 'APPROVED', NOW(), NOW()
FROM seq;

-- 4. 맛집-카테고리 매핑 (1곳당 2개씩)
INSERT INTO place_category (place_id, category_id, display_order)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT * FROM (
                  SELECT n AS place_id, 1 + MOD(n - 1, 3) AS category_id, 0 AS display_order FROM seq
                  UNION ALL
                  SELECT n AS place_id, 1 + MOD(n, 3) AS category_id, 1 AS display_order FROM seq
              ) AS t;

-- 5. 맛집-태그 매핑 (1곳당 2개씩)
INSERT INTO place_tag (place_id, tag_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT * FROM (
                  SELECT n AS place_id, 1 + MOD(n - 1, 3) AS tag_id FROM seq
                  UNION ALL
                  SELECT n AS place_id, 1 + MOD(n, 3) AS tag_id FROM seq
              ) AS t;

INSERT INTO photo (place_id, photo_url, display_order, fetched_at, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT * FROM (
                  SELECT n, CONCAT('https://cdn.example.com/place/', n, '/photo-1.jpg') AS url, 1 AS ord,
                         NOW() AS f_at, NOW() AS c_at, NOW() AS u_at FROM seq
                  UNION ALL
                  SELECT n, CONCAT('https://cdn.example.com/place/', n, '/photo-2.jpg'), 2, NOW(), NOW(), NOW() FROM seq
              ) AS t;

-- 7. 메뉴 데이터 생성 (NOW() 별칭 추가로 에러 해결)
INSERT INTO menu (place_id, name, price, is_recommended, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT * FROM (
                  SELECT n, CONCAT('대표메뉴-', n, '-1') AS m_name, 8000 + MOD(n, 5) * 500 AS p, TRUE AS rec,
                         NOW() AS c_at, NOW() AS u_at FROM seq
                  UNION ALL
                  SELECT n, CONCAT('대표메뉴-', n, '-2'), 9000 + MOD(n, 7) * 400, FALSE, NOW(), NOW() FROM seq
                  UNION ALL
                  SELECT n, CONCAT('대표메뉴-', n, '-3'), 10000 + MOD(n, 9) * 300, FALSE, NOW(), NOW() FROM seq
              ) AS t;

-- 8. AUTO_INCREMENT 값 조정
ALTER TABLE category AUTO_INCREMENT = 4;
ALTER TABLE tag AUTO_INCREMENT = 4;
ALTER TABLE place AUTO_INCREMENT = 1001;
