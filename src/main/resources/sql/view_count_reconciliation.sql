-- 조회수 원본 로그(place_view_log) 및 일별 검증 보정 결과(view_count_reconciliation) 참고 스크립트
-- 적재 경로: PlaceReadService.getPlaceDetail 이 요청 트랜잭션에서 place_view_log 를 동기 적재
-- 검증 보정: ViewCountReconciliationScheduler 가 매일 04:00(KST) 전날까지 로그와 daily_view_count 를 비교해 보정

-- =====================================================================
-- [필수] prod 배포 전 수동 DDL
-- prod 는 ddl-auto: validate 이므로 이 테이블들을 먼저 생성하지 않으면 기동에 실패한다.
-- (local/test 는 자동 생성) 컬럼 정의는 엔티티 매핑과 일치시켜 validate 를 통과시킨다.
-- 배포 후 기동 로그에 스키마 validate 에러가 없는지 확인할 것.
-- =====================================================================
CREATE TABLE place_view_log (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    place_id    BIGINT      NOT NULL,
    user_id     BIGINT      NULL,
    view_date   DATE        NOT NULL,
    created_at  DATETIME(6) NULL,
    updated_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_place_view_log_date_place (view_date, place_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE view_count_reconciliation (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    target_date       DATE        NOT NULL,
    logged_count      BIGINT      NOT NULL,
    aggregated_count  BIGINT      NOT NULL,
    missing_count     BIGINT      NOT NULL,
    over_count        BIGINT      NOT NULL,
    created_at        DATETIME(6) NULL,
    updated_at        DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_view_count_reconciliation_target_date (target_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
-- 조회 쿼리
-- =====================================================================

-- 1) 최근 30일 일별 누락률
SELECT target_date,
       logged_count,
       aggregated_count,
       missing_count,
       over_count,
       ROUND(missing_count / NULLIF(logged_count, 0) * 100, 2) AS loss_rate_pct
FROM view_count_reconciliation
ORDER BY target_date DESC
LIMIT 30;

-- 2) 아직 보정되지 않은 오늘 기준, 맛집별 원본 로그와 집계 비교
SELECT l.place_id,
       l.logged,
       COALESCE(d.aggregated, 0) AS aggregated,
       l.logged - COALESCE(d.aggregated, 0) AS delta
FROM (SELECT place_id, COUNT(*) AS logged
      FROM place_view_log
      WHERE view_date = CURDATE()
      GROUP BY place_id) l
LEFT JOIN (SELECT place_id, SUM(count) AS aggregated
           FROM daily_view_count
           WHERE view_date = CURDATE()
           GROUP BY place_id) d ON d.place_id = l.place_id
ORDER BY delta DESC;
