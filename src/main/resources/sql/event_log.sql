-- 비즈니스 이벤트 로깅(event_log) 참고 스크립트
-- GA4(클라이언트 자동수집)가 못 보거나 DB 조인이 필요한 비즈니스 이벤트를 서버에서 적재한다.
-- 적재 경로: common/analytics 의 AnalyticsRecorder(발행) -> AnalyticsEventListener(비동기 AFTER_COMMIT 적재)

-- =====================================================================
-- [필수] prod 배포 전 수동 DDL
-- prod 는 ddl-auto: validate 이므로 이 테이블을 먼저 생성하지 않으면 기동에 실패한다.
-- (local/test 는 자동 생성) 컬럼 정의는 엔티티 매핑과 일치시켜 validate 를 통과시킨다.
-- 배포 후 기동 로그에 스키마 validate 에러가 없는지 확인할 것.
-- =====================================================================
CREATE TABLE event_log (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    event_type    VARCHAR(50)   NOT NULL,
    user_id       BIGINT        NULL,
    anonymous_id  VARCHAR(100)  NULL,
    target_type   VARCHAR(50)   NULL,
    target_id     BIGINT        NULL,
    properties    VARCHAR(4000) NULL,
    occurred_at   DATETIME(6)   NOT NULL,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_event_log_type_occurred (event_type, occurred_at),
    KEY idx_event_log_user_occurred (user_id, occurred_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 적재 이벤트(MVP): SIGNUP, LOGIN, PLACE_REGISTER_REQUESTED,
--                  PLACE_APPROVED, PLACE_REJECTED, EVENT_ENTERED

-- =====================================================================
-- 검증용 집계 쿼리
-- =====================================================================

-- 1) 일별 신규 가입 추이 (획득)
SELECT DATE(occurred_at) AS d, COUNT(*) AS signups
FROM event_log
WHERE event_type = 'SIGNUP'
GROUP BY DATE(occurred_at)
ORDER BY d;

-- 2) 일별 DAU (로그인 기준, 회원 리텐션의 기반)
SELECT DATE(occurred_at) AS d, COUNT(DISTINCT user_id) AS dau
FROM event_log
WHERE event_type = 'LOGIN'
GROUP BY DATE(occurred_at)
ORDER BY d;

-- 3) 가입 주차 대비 7일 이후 재로그인 (주간 코호트 리텐션 간이 버전)
WITH signup AS (
    SELECT user_id, MIN(occurred_at) AS signup_at
    FROM event_log WHERE event_type = 'SIGNUP' GROUP BY user_id
)
SELECT
    YEARWEEK(s.signup_at, 3)                                     AS signup_week,
    COUNT(DISTINCT s.user_id)                                    AS signups,
    COUNT(DISTINCT CASE WHEN l.occurred_at >= s.signup_at + INTERVAL 7 DAY
                        THEN l.user_id END)                      AS relogin_after_7d
FROM signup s
LEFT JOIN event_log l ON l.user_id = s.user_id AND l.event_type = 'LOGIN'
GROUP BY YEARWEEK(s.signup_at, 3)
ORDER BY signup_week;

-- 4) 등록 -> 승인/반려 -> 응모 퍼널
SELECT event_type, COUNT(*) AS cnt
FROM event_log
WHERE event_type IN ('PLACE_REGISTER_REQUESTED', 'PLACE_APPROVED', 'PLACE_REJECTED', 'EVENT_ENTERED')
GROUP BY event_type;

-- 5) 이벤트 응모를 캠퍼스별로 (GA 가 못 하는 비즈니스 조인 예시)
SELECT p.campus, COUNT(*) AS entries
FROM event_log e
JOIN place p ON e.target_type = 'PLACE' AND e.target_id = p.id
WHERE e.event_type = 'EVENT_ENTERED'
GROUP BY p.campus;
