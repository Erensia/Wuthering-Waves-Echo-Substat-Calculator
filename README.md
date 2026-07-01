# 명조 에코 점수 계산기

Spring Boot와 PostgreSQL로 만든 에코 점수 계산기입니다. 별도의 Node.js 설치가 필요하지 않습니다.

## 실행

필요 환경:

- Java 21
- PostgreSQL 17 또는 Docker Desktop

Docker를 사용하는 경우:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

STS에서는 `File > Import > Existing Maven Projects`로 이 폴더를 불러온 뒤
`EchoGraderApplication`을 Spring Boot App으로 실행합니다.

기본 접속 주소는 `http://localhost:8080`입니다.

## DB 접속 설정

기본 URL은 `compose.yaml`과 동일합니다. DB 계정 정보는 저장소에 커밋하지 않습니다.
다음 중 한 가지 방식으로 설정합니다.

1. `application-local.properties.example`을 `application-local.properties`로 복사한 뒤 로컬 값을 입력합니다.
   이 파일은 Git에서 제외됩니다.
2. 환경 변수를 지정합니다.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/echo_grader"
$env:DB_USERNAME="echo_user"
$env:DB_PASSWORD="echo_password"
```

Flyway가 애플리케이션 시작 시 테이블을 생성합니다. 점수 계산은 DB와 독립적이고,
화면의 저장 버튼 또는 `POST /api/v1/loadouts` API로 결과를 `loadout`, `echo_stat` 테이블에 저장합니다.

## 계산 API

`POST /api/v1/scores/calculate`

```json
{
  "firstEchoMainStat": "CRIT_RATE",
  "echoes": [
    {"critRate": 8.7, "critDamage": 17.4},
    {"critRate": 9.3, "critDamage": 18.6},
    {"critRate": 8.1, "critDamage": 15.0},
    {"critRate": 7.5, "critDamage": 14.2},
    {"critRate": 8.5, "critDamage": 16.2}
  ]
}
```
