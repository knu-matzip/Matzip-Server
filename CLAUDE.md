# KNU-Matzip Server

이 저장소에서 작업할 때 지키는 규칙을 정리한다. 사용자의 명시적 지시가 있으면 그것을 우선한다.

KNU-Matzip 백엔드. Spring Boot 3.4 (Java 17) + JPA + MySQL.

## 구조

단일 Gradle 모듈이며 `com.matzip` 아래를 **도메인별로 나눈(package-by-domain)** 구조다.
도메인 패키지 내부는 **고전 3계층 레이어드 아키텍처**로 통일한다. 의존은 위에서 아래로 단방향, 직접 호출한다.

```text
controller -> service -> repository -> DB
   (client: DB가 아닌 외부 연동 어댑터 — service가 직접 호출)
```

- 도메인 패키지: `auth`(카카오 로그인/JWT 발급), `user`(프로필), `place`(맛집 등록·조회·찜·검색), `lottery`(응모 이벤트), `admin`(맛집 승인/거절), `common`(공통).
- **하위 계층이 상위 계층에 정의된 인터페이스(port)를 구현하는 의존성 역전 구조를 쓰지 않는다** (헥사고날 아님). 인터페이스가 필요하면 그 인터페이스는 자신이 속한 계층에 둔다.
- 도메인 패키지 내부 표준:
  - `controller/` — Presentation. `@RestController`. 요청/응답 바디는 `dto/`의 DTO 사용.
  - `service/` — Business. `@Service` 구현체. **인터페이스 없이 구현체만 둔다** (실제 교체가 필요한 예외에만 인터페이스 추가, 그 인터페이스도 이 계층 소유).
  - `repository/` — Data Access. Spring Data JPA 리포지토리(인터페이스 = DAO, 구현은 프레임워크 생성). DB와 직접 통신.
  - `domain/` — 도메인 모델. JPA 엔티티(`domain/entity/`)와 enum/값 타입. 여러 계층에서 공유.
  - `dto/` — 전송 객체. `dto/request/`, `dto/response/`로 나눈다. 요청/응답 양쪽이 공유하는 조각은 `dto/` 루트에 둔다.
  - `client/` (필요할 때만) — DB가 아닌 외부 연동 어댑터(외부 API 클라이언트·캐시 등). 외부 응답 파싱 DTO는 `client/.../dto`.
  - `event/` (필요할 때만) — 스프링 애플리케이션 이벤트/리스너. 특정 계층에 속하지 않는 교차 관심사라 전용 패키지로 둔다.

## 구현 규칙

### 컨트롤러와 응답

- 모든 컨트롤러 응답은 `common/response/ApiResponse<T>`로 감싼다. 팩토리: `ApiResponse.success(data)`, `successWithoutData()`(DELETE/PATCH 등), `successWithEmptyList()`. 응답 형태는 `{ status, timestamp, data, error }`.
- 비즈니스 예외는 `BusinessException`(+ `ErrorCode` enum)을 던지고, `GlobalExceptionHandler`가 중앙에서 `ApiResponse` 에러 형태로 변환한다. **컨트롤러에서 try/catch 하지 않는다.**

### 인증

- Stateless JWT. `JwtAuthenticationFilter`가 토큰을 검증해 `UserPrincipal`을 세팅한다(세션 없음, CSRF off).
- `SecurityConfig`상 대부분 엔드포인트는 `permitAll`, 일부 이벤트 응모 경로만 인증 필요.
- 인증 여부는 컨트롤러/서비스단에서 `UserPrincipal` 유무로 분기한다.

### 서비스

- `service/`에는 `@Service` 구현체만 둔다. 상위(controller)가 하위(service, repository)를 직접 호출한다.
- 외부 provider·캐시 등 DB가 아닌 연동은 `client/`의 어댑터를 service가 직접 사용한다.

### 영속성

- JPA 엔티티는 `domain/entity/`에 둔다.
- 모든 엔티티는 `common/entity/BaseEntity`를 상속해 `createdAt`/`updatedAt` 감사(auditing)를 일관되게 적용한다(`@MappedSuperclass` + `AuditingEntityListener`, PK는 `IDENTITY`).
- 스키마 반영은 프로파일을 따른다: `local`은 `ddl-auto: update`, `prod`는 `validate`.

### DTO

- 모든 DTO는 `Dto` 접미사를 붙인다. 유일한 예외는 응답 래퍼 `ApiResponse`.
- HTTP 요청/응답 DTO는 `dto/request`·`dto/response`. 요청/응답이 공유하는 조각은 `dto/` 루트. 외부 API 응답 파싱 DTO는 해당 `client/.../dto`.
- 프레임워크·엔티티 타입을 응답 DTO 대신 그대로 노출하지 않는다.

### 설정과 알림

- 프로퍼티는 `@ConfigurationProperties` 클래스(`common/config/`의 `JwtProperties`, `KakaoProperties`, `ImageProperties`, `DiscordWebhookProperties` 등)로 바인딩한다.
- 맛집 등록/당첨 알림은 `common/infra/discord/DiscordWebhookSender`. 비동기는 `AsyncConfiguration`, 스케줄러는 `SchedulerConfiguration`.

## 작업 방식

1. 작업 전에 관련 코드, 테스트, 설정을 읽고 현재 동작을 근거로 삼는다.
2. 새 작업 이슈는 GitHub Issues에 생성하고, 이슈 번호를 브랜치/PR 맥락에 사용한다(예: `refactor/142-place-layered`).
3. 구현 전에 사용자에게 작업 계획을 먼저 공유하고 확인을 받는다.
4. PR을 올리기 전에 변경 내용과 검증 결과를 사용자에게 먼저 리뷰받는다.
5. 요구사항을 추측해 메우지 않는다. 모호함이 결과나 공개 계약(엔드포인트·응답 형태)을 바꾸면 선택지와 영향을 명시하고 확인한다.
6. 요구 범위를 충족하는 최소 변경만 한다.
7. 변경 위험에 맞는 테스트를 추가/수정하고 검증한다. 검증하지 못한 항목을 완료로 표현하지 않는다.

## 실행

- 로컬 실행: `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`
- 로컬 MySQL(`localhost:3306/knu_matzip`)과 env var가 필요하다: `DB_USER`, `DB_PASS`, `JWT_SECRET_KEY`, `KAKAO_CLIENT_ID`, `STATE_SECRET_KEY`.

## 검증

- 빌드(테스트 포함): `./gradlew build`
- 전체 테스트: `./gradlew test`
- 단일 테스트: `./gradlew test --tests "com.matzip.place.service.PlaceReadServiceTest"` (메서드까지: `...PlaceServiceTest.메서드명`)
- 테스트는 `src/test/resources/application.yml`의 H2 인메모리 + 더미 시크릿으로 완결적이라 외부 DB·시크릿 없이 돈다.

## 커밋

- 커밋 메시지는 `type: 제목` 형식으로 쓴다. 예: `refactor: place 도메인을 레이어드 표준 패키지로 통일`
- `type`: `feat`, `fix`, `refactor`, `chore`, `docs`
- 제목은 한국어로 간결하게 쓰고 끝에 마침표를 붙이지 않는다.
- 하나의 커밋은 하나의 논리적 변경만 담는다.

## Pull Request

- PR base branch는 항상 `develop`으로 잡는다. `main` push가 운영 배포(EC2) 트리거다.
- PR 제목은 이슈 유형 접두사를 붙인다(예: `[refactor] ...`, `[feature] ...`).
- PR 본문은 `.github/pull_request_template.md` 형식(작업 내용, 체크리스트, 관련 이슈)을 따르고 `close #이슈번호`로 연결한다.