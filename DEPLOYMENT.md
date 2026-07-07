# 배포 가이드

이 문서는 `echo-grader`를 웹 애플리케이션으로 배포하기 위한 최소 절차입니다.

## 1. 배포 전 확인

- Java 21 기준으로 빌드합니다.
- 운영 DB는 PostgreSQL 18 기준입니다.
- 운영 환경에서는 `SPRING_PROFILES_ACTIVE=prod`를 사용합니다.
- HTTPS 리버스 프록시 뒤에서 실행해야 합니다. `prod` 프로필은 보안 쿠키를 활성화하므로 HTTP 단독 운영에서는 로그인 세션이 정상 동작하지 않습니다.

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## 2. 필수 환경 변수

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<db-host>:5432/echo_grader
DB_USERNAME=<db-user>
DB_PASSWORD=<db-password>
```

비밀번호는 Git에 커밋하지 말고 배포 환경의 secret, 환경 변수, 또는 호스팅 플랫폼 설정으로 주입합니다.

## 3. JAR 직접 실행

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://db-host:5432/echo_grader"
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="<secret>"
java -jar target/echo-grader-1.0.0.jar
```

## 4. Docker 이미지 빌드

```powershell
docker build -t echo-grader:1.0.0 .
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5433/echo_grader" `
  -e DB_USERNAME="echo_user" `
  -e DB_PASSWORD="<secret>" `
  echo-grader:1.0.0
```

## 5. 단일 서버 Docker Compose 예시

`compose.prod.yaml`은 작은 VPS나 테스트용 운영 환경에서 앱과 PostgreSQL을 함께 띄우는 예시입니다.
실서비스에서는 관리형 PostgreSQL과 별도 백업 정책을 우선 검토하는 편이 낫습니다.

```powershell
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="<secret>"
docker compose -f compose.prod.yaml up -d --build
```

애플리케이션은 서버 내부의 `127.0.0.1:8080`에만 노출됩니다. Nginx, Caddy, Cloudflare Tunnel, 로드밸런서 중 하나로 HTTPS를 종료하고 앱으로 프록시합니다.

프록시가 전달해야 하는 헤더:

```text
X-Forwarded-For
X-Forwarded-Proto
X-Forwarded-Host
```

## 6. 운영 확인

배포 후 다음을 확인합니다.

```text
GET /health
GET /
POST /api/v1/scores/calculate
```

로그인/회원가입 요청 제한은 현재 애플리케이션 인스턴스 메모리 기준입니다. 여러 인스턴스로 확장할 때는 Redis, API Gateway, 또는 로드밸런서 레벨 rate limit으로 교체해야 합니다.
