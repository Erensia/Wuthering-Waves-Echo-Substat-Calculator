codex 바이브 코딩 연습용

# 명조 에코 점수 계산기

Spring Boot와 PostgreSQL로 만든 명조 에코 점수 계산기입니다.

현재 버전: **1.0.0**

## 1.0.0 변경 사항

- Spring Security 기반 세션 인증과 보호 API 접근 제어를 적용했습니다.
- 상태 변경 API에 CSRF 토큰 검증을 적용하고 프런트엔드에서 토큰을 자동 처리합니다.
- 로그인 시 세션 ID를 교체하고 CSP, Referrer-Policy 등 보안 응답 헤더를 추가했습니다.
- 로그인은 IP당 분당 10회, 회원가입은 시간당 5회, 회원검색은 분당 60회로 제한합니다.
- 신규 비밀번호의 최소 길이를 8자로 강화했습니다.
- 회원검색을 로그인 사용자에게만 허용합니다.
- 운영용 `prod` 프로필에 상세 오류 숨김, 보안 쿠키, 프록시 헤더, 30분 세션 만료,
  graceful shutdown을 적용했습니다.
- 정적 파일에 버전을 부여하여 배포 후 이전 JavaScript가 캐시에 남는 문제를 방지합니다.
- 보안 설정, CSRF, 접근 제어, 세션 교체, 요청 제한을 포함한 테스트 23개를 통과합니다.

요청 제한 정보는 애플리케이션 인스턴스 메모리에 저장됩니다.
여러 인스턴스로 확장할 때는 Redis 또는 API Gateway 기반 제한으로 교체해야 합니다.

## 실행

필요 환경:

- Java 21
- PostgreSQL 18 또는 Docker Desktop

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
$env:DB_URL="jdbc:postgresql://localhost:5433/echo_grader"
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
CSRF 방어, 로그인·회원가입 요청 제한, 세션 ID 교체와 보안 응답 헤더를 적용합니다.
운영 환경에서는 HTTPS 적용이 필요합니다.

## 운영 프로필

운영 환경에서는 `prod` 프로필을 활성화하고 DB 접속 정보를 환경 변수로 주입합니다.
상세 배포 절차는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고합니다.

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://db-host:5432/echo_grader"
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="<운영 비밀번호>"
java -jar target/echo-grader-1.0.0.jar
```

`prod` 프로필은 상세 오류 응답과 템플릿 개발 모드를 끄고 보안 세션 쿠키,
프록시 전달 헤더, 30분 세션 만료, graceful shutdown을 적용합니다.
보안 쿠키는 HTTPS에서만 전송되므로 운영 환경은 반드시 TLS를 통해 서비스해야 합니다.
컨테이너 배포용 `Dockerfile`과 단일 서버 예시인 `compose.prod.yaml`을 제공합니다.

## API

- `GET /api/v1/csrf`: 상태 변경 요청용 CSRF 토큰 발급
- `GET /health`: 배포 헬스체크
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
