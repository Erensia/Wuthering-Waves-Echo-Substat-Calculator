# 명조 에코 점수 계산기 (Echo Grader)

명조(Wuthering Waves)의 에코(Echo) 세트 서브옵션을 점수화하고, 회원별로 계산 결과를 저장·관리할 수 있는 Spring Boot 웹 애플리케이션입니다.

## 기술 스택

- **Backend:** Java 21, Spring Boot 4.1
- **DB / 마이그레이션:** PostgreSQL 17, Flyway
- **View:** Thymeleaf
- **Security:** Spring Security Crypto (BCrypt)
- **Test:** JUnit 5
- **Infra:** Docker Compose (로컬 PostgreSQL 구동)

## 프로젝트 구조

기능 단위로 패키지를 분리해 각 도메인의 책임을 명확히 했습니다.

```
com.wuwa.echograder
├── auth       # 회원가입 / 로그인 / 세션 인증
├── loadout    # 에코 세트 저장 및 조회
├── score      # 에코 점수 계산 로직
└── web        # Controller 계층 (API 엔드포인트)
```

Controller — Service — Repository 계층을 분리해 각 계층이 단일 책임만 갖도록 구성했고, 도메인 로직(점수 계산, 회원 인증)과 웹 계층을 분리해 테스트 용이성을 확보했습니다.

## 핵심 기능

- **비밀번호 보안:** BCrypt 단방향 해시로 저장하며, 기존 평문 비밀번호 데이터도 Flyway 마이그레이션(`V4__hash_user_passwords`)으로 안전하게 전환합니다.
- **세션 기반 인증:** 회원가입 시 자동 로그인, 서버 세션으로 로그인 상태 유지.
- **에코 점수 계산 및 저장:** 슬롯별 코스트, 치명타 확률·피해 등 서브옵션을 계산하고, 결과를 회원별로 저장·재조회할 수 있습니다.
- **데이터 무결성:** Flyway로 스키마 버전을 관리해 기존 데이터 손실 없이 컬럼/제약조건을 변경합니다.

## API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| GET | `/api/v1/auth/me` | 현재 로그인 회원 조회 |
| POST | `/api/v1/scores/calculate` | 에코 점수 계산 |
| POST | `/api/v1/loadouts` | 현재 회원의 세트 저장 |
| GET | `/api/v1/loadouts` | 현재 회원의 저장 세트 조회 |

## 실행 방법

필요 환경: Java 21, PostgreSQL 17 또는 Docker Desktop

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

브라우저에서 `http://localhost:8080` 접속.

### DB 설정

`application-local.properties.example`을 `application-local.properties`로 복사하거나 아래 환경 변수를 설정합니다.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/echo_grader"
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="echo_password"
```

### 테스트

```powershell
.\mvnw.cmd test
```

## 알려진 제한 사항

외부 공개 전 CSRF 방어, 로그인 시도 제한, HTTPS 적용이 추가로 필요합니다.
