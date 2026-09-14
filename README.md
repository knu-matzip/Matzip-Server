# KNU-Matzip | 공주대학교 학생들을 위한 맛집 서비스

> 캠퍼스 주변의 맛집들을 지금 바로 찾아보세요!

**[🔗 서비스 바로가기](https://knu-matzip.vercel.app/)**

<img width="1920" alt="KNU-Matzip" src="https://github.com/user-attachments/assets/ca63e59d-13b6-44bb-acca-d24f7feb3a88" />

<br/>

## 📖 프로젝트 소개

**KNU-Matzip 서버**는 공주대학교 학생들을 위한 캠퍼스 맛집 큐레이션 서비스의 API 서버입니다.

- **기간**: 2025.09 ~ 현재 (운영 중)
- **역할**: 백엔드 설계 및 개발

#### 문제 정의

> 학교 주변 맛집 정보는 네이버 지도·검색으로 쉽게 찾을 수 있지만, 광고·협찬성 콘텐츠가 섞여 신뢰하기 어렵습니다. 그래서 학생들은 실제 경험 기반 후기를 얻으려 에브리타임을 찾지만, 후기는 시간이 지나면 밀려 사라지고 여러 게시글에 흩어져 있어 원하는 맛집 하나를 찾기 위해 매번 반복 검색해야 합니다.
>
> <img width="200" alt="문제 정의" src="https://github.com/user-attachments/assets/ad2cd326-e741-44a8-a89e-2d70655eee7a" />

<br/>

## ⚙️ 기술 스택

**Back-end**

![Back-end](https://go-skill-icons.vercel.app/api/icons?i=java,spring,mysql,aws)

- **Language / Framework**: Java 17, Spring Boot 3.4 (Web, Data JPA, Security, Validation)
- **Auth**: Kakao OAuth2 + Stateless JWT (`jjwt`)
- **DB**: MySQL (운영) / H2 (테스트)
- **관측성**: Spring Boot Actuator, Prometheus, Grafana
- **알림**: Discord Webhook
- **Infra**: AWS EC2, Nginx

<details>
<summary>Front-end 스택 (별도 저장소)</summary>

![Front-end](https://go-skill-icons.vercel.app/api/icons?i=ts,nextjs,zustand,reactquery,tailwind,storybook,pnpm,turborepo,googleanalytics,sentry&perline=5)

</details>

<br/>

## 🏛️ 아키텍처

<img width="1000" alt="Back-end Architecture" src="https://github.com/user-attachments/assets/18840a9c-4d9d-4b55-9344-57d201d0a89e" />

### 패키지 구조

기능이 늘어날수록 계층 우선(`controller/`, `service/` …) 구조는 하나의 변경이 여러 패키지에 흩어지는 문제가 있습니다.
그래서 **도메인 단위로 먼저 나누고**, 각 도메인 내부를 **3계층 레이어드**로 통일했습니다.

```text
com.matzip
├── auth      # 카카오 로그인 / JWT 발급·재발급
├── user      # 프로필
├── place     # 맛집 등록·조회·찜·검색
├── lottery   # 럭키 드로우 응모 이벤트
├── admin     # 맛집 승인·거절
└── common    # 응답·예외·보안·설정·로깅·분석 등 공통

# 각 도메인 내부
controller → service → repository → DB
                    ↘ client (외부 API·캐시 어댑터)
```

## ERD
<img width="1000" alt="knu-matzip-erd" src="https://github.com/user-attachments/assets/912e44c8-7cd8-4363-aae3-a79cf1c747dc" />


## 📱 주요 기능

| 기능 | 설명                                      |
| --- |-----------------------------------------|
| **맛집 탐색** | 카테고리·지도로 캠퍼스 근처 맛집 조회, 찜, 키워드 검색        |
| **맛집 등록** | 캠퍼스·식당·추천 메뉴·태그·카테고리를 단계별로 입력해 등록 신청    |
| **관리자 승인** | 등록 신청된 맛집을 관리자가 승인·거절 (승인 시 Discord 알림) |
| **럭키 드로우** | 맛집 등록으로 얻은 응모권으로 기프티콘 이벤트 참여, 스케줄러 추첨   |
| **오늘 뭐 먹지** | 랜덤 카테고리 추천                              |
| **내 정보** | 등록 현황·기프티콘 조회                           |


## 📸 화면 스크린샷 보기

**맛집 탐색**
<p>
<img width="180" alt="탐색1" src="https://github.com/user-attachments/assets/48b5e7d1-9184-4ac6-99c7-cf1491028635" />
<img width="180" alt="탐색2" src="https://github.com/user-attachments/assets/24a1a29d-abb8-4136-9e09-4c869a73acaa" />
<img width="180" alt="탐색3" src="https://github.com/user-attachments/assets/a4fe04d5-0569-45b0-a657-7298e193e176" />
<img width="180" alt="탐색4" src="https://github.com/user-attachments/assets/d12e25c2-a218-49ae-bd5c-67f0aa59df40" />
</p>

**맛집 등록**
<p>
<img width="180" alt="등록1" src="https://github.com/user-attachments/assets/4f5f95f4-5de5-44c1-a79c-2f77498bf9a0" />
<img width="180" alt="등록2" src="https://github.com/user-attachments/assets/09c1fe01-94d6-42ff-a2bc-63ee4a623dcd" />
<img width="180" alt="등록3" src="https://github.com/user-attachments/assets/29786b0c-8037-4062-8e17-dd12e2a139bf" />
<img width="180" alt="등록4" src="https://github.com/user-attachments/assets/88ee31e7-b8dc-4b68-a63e-1a94e69fc5d6" />
</p>

**럭키 드로우 이벤트**
<p>
<img width="180" alt="이벤트1" src="https://github.com/user-attachments/assets/50942ccc-9934-494f-b470-778de640b940" />
<img width="180" alt="이벤트2" src="https://github.com/user-attachments/assets/3a9e223b-1dd8-47a4-96c7-52d7b7481937" />
<img width="180" alt="이벤트3" src="https://github.com/user-attachments/assets/cf2fb97f-5b16-44c7-945e-690ed75d518a" />
<img width="180" alt="이벤트4" src="https://github.com/user-attachments/assets/d83f89be-7ad5-4536-b594-8f4d07358399" />
</p>

**오늘 뭐 먹지 / 내 정보**
<p>
<img width="180" alt="추천" src="https://github.com/user-attachments/assets/0b4bd0f0-a0c7-4a01-a1eb-5429e2241ffb" />
<img width="180" alt="내정보1" src="https://github.com/user-attachments/assets/71f28830-0d27-4b7b-87e2-fb72772417f1" />
<img width="180" alt="내정보2" src="https://github.com/user-attachments/assets/7d5316b1-c7fc-4588-b0dd-66098cfe499c" />
<img width="180" alt="내정보3" src="https://github.com/user-attachments/assets/43c96503-7118-4001-a67d-edf5028ef507" />
</p>

<br/>

## 🚀 로컬 실행

```bash
# 1. 로컬 MySQL 준비 (localhost:3306/knu_matzip)

# 2. 환경 변수 설정
export DB_USER=... DB_PASS=... JWT_SECRET_KEY=... KAKAO_CLIENT_ID=... STATE_SECRET_KEY=...

# 3. 실행 (local 프로파일)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

**검증**

```bash
./gradlew build   # 빌드 + 테스트
./gradlew test    # 전체 테스트 (H2 인메모리 + 더미 시크릿으로 외부 의존 없이 동작)
```

- 스키마 반영: `local`은 `ddl-auto: update`, `prod`는 `validate`
- 배포: `main` 브랜치 push 시 EC2 자동 배포
