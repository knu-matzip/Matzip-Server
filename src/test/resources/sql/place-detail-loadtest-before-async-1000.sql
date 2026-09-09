SET SESSION cte_max_recursion_depth = 2000;
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

INSERT INTO category (category_id, category_name, icon_key)
VALUES
    (1, '한식', 'korean'),
    (2, '중식', 'chinese'),
    (3, '일식', 'japanese');

INSERT INTO tag (tag_id, tag_name, icon_key)
VALUES
    (1, '가성비', 'value'),
    (2, '혼밥', 'solo'),
    (3, '웨이팅', 'waiting');

INSERT INTO place (
    place_id,
    campus,
    kakao_place_id,
    place_name,
    address,
    latitude,
    longitude,
    description,
    like_count,
    view_count,
    registered_by,
    status,
    created_at,
    updated_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    n,
    'CHEONAN',
    CONCAT('before-async-kakao-', LPAD(n, 4, '0')),
    CONCAT('비동기 분리 전 맛집 ', n),
    CONCAT('충남 천안시 회귀테스트로 ', n),
    36.800000 + (n * 0.0001),
    127.100000 + (n * 0.0001),
    CONCAT('동기 조회수 갱신 비교용 테스트 데이터 #', n),
    MOD(n, 200),
    100 + MOD(n, 500),
    NULL,
    'APPROVED',
    NOW(),
    NOW()
FROM seq;

INSERT INTO daily_view_count (
    id,
    place_id,
    view_date,
    count,
    created_at,
    updated_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    n,
    n,
    CURRENT_DATE(),
    10 + MOD(n, 300),
    NOW(),
    NOW()
FROM seq;

INSERT INTO place_category (place_id, category_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT n, 1 + MOD(n - 1, 3) FROM seq
UNION ALL
SELECT n, 1 + MOD(n, 3) FROM seq;

INSERT INTO place_tag (place_id, tag_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT n, 1 + MOD(n - 1, 3) FROM seq
UNION ALL
SELECT n, 1 + MOD(n, 3) FROM seq;

INSERT INTO photo (
    place_id,
    photo_url,
    display_order,
    created_at,
    fetched_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    n,
    CONCAT('https://cdn.example.com/before-async/place/', n, '/photo-1.jpg'),
    1,
    NOW(),
    NOW()
FROM seq
UNION ALL
SELECT
    n,
    CONCAT('https://cdn.example.com/before-async/place/', n, '/photo-2.jpg'),
    2,
    NOW(),
    NOW()
FROM seq;

INSERT INTO menu (
    place_id,
    name,
    price,
    is_recommended
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    n,
    CONCAT('동기메뉴-', n, '-1'),
    8000 + MOD(n, 5) * 500,
    TRUE
FROM seq
UNION ALL
SELECT
    n,
    CONCAT('동기메뉴-', n, '-2'),
    9000 + MOD(n, 7) * 400,
    FALSE
FROM seq
UNION ALL
SELECT
    n,
    CONCAT('동기메뉴-', n, '-3'),
    10000 + MOD(n, 9) * 300,
    FALSE
FROM seq;

ALTER TABLE category AUTO_INCREMENT = 4;
ALTER TABLE tag AUTO_INCREMENT = 4;
ALTER TABLE place AUTO_INCREMENT = 1001;
ALTER TABLE daily_view_count AUTO_INCREMENT = 1001;

