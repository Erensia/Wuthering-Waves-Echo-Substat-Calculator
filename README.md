codex 바이브 코딩 연습용

# 명조 에코 점수 계산기

Spring Boot와 PostgreSQL로 만든 명조 에코 점수 계산기입니다.

## 실행

필요 환경:

- Java 21
- PostgreSQL 17 또는 Docker Desktop

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

브라우저에서 `http://localhost:8080`을 엽니다.

STS에서는 `File > Import > Existing Maven Projects`로 불러온 뒤
`EchoGraderApplication`을 Spring Boot App으로 실행할 수 있습니다.

## DB 설정

`application-local.properties.example`을 `application-local.properties`로 복사하거나
다음 환경 변수를 설정합니다.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/echo_grader"
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="echo_password"
```

Flyway가 실행 시점에 회원, 에코 세트, 에코 부옵션 테이블을 자동으로 구성합니다.
기존 V1/V2 데이터는 삭제하지 않으며, 소유자가 없는 기존 세트는 회원별 목록에 노출되지 않습니다.

## 회원과 저장 기능

- 회원가입 시 바로 로그인됩니다.
- 로그인 상태는 서버 세션으로 유지됩니다.
- 계산한 에코 세트를 현재 회원에게 귀속하여 저장합니다.
- 저장 목록에서 슬롯별 코스트, 치명타 확률, 치명타 피해를 확인하고 계산기로 다시 불러올 수 있습니다.

비밀번호는 BCrypt 단방향 해시로 DB에 저장하며, 기존 평문 비밀번호도 Flyway 마이그레이션으로 변환합니다.
외부 공개 전에는 CSRF 방어, 로그인 시도 제한, HTTPS 적용이 필요합니다.

## API

- `POST /api/v1/auth/signup`: 회원가입
- `POST /api/v1/auth/login`: 로그인
- `POST /api/v1/auth/logout`: 로그아웃
- `GET /api/v1/auth/me`: 현재 로그인 회원
- `POST /api/v1/scores/calculate`: 에코 점수 계산
- `POST /api/v1/loadouts`: 현재 회원의 세트 저장
- `GET /api/v1/loadouts`: 현재 회원의 저장 세트 조회

## 테스트

```powershell
.\mvnw.cmd test
```
