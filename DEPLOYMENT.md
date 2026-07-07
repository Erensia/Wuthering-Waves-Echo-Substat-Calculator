# 정적 웹 배포 가이드

이 프로젝트는 순수 정적 웹페이지입니다. 서버, DB, Docker, Java 런타임이 필요 없습니다.

## GitHub Pages

1. 변경 사항을 GitHub 브랜치에 푸시합니다.
2. 저장소의 `Settings > Pages`로 이동합니다.
3. `Build and deployment`에서 `Deploy from a branch`를 선택합니다.
4. 배포할 브랜치와 `/ (root)`를 선택합니다.
5. 저장 후 생성되는 Pages URL로 접속합니다.

## 다른 정적 호스팅

아래 서비스에는 저장소 루트를 그대로 배포하면 됩니다.

- Cloudflare Pages
- Netlify
- Vercel

빌드 명령어는 비워두고, 출력 디렉터리는 저장소 루트 또는 서비스 기본값을 사용하면 됩니다.

## 파일 구조

```text
index.html
css/app.css
js/app.js
```

계산과 백업/복원은 `js/app.js`에서 브라우저 내에서만 수행됩니다. 개인정보, 로그인 세션, 서버 저장 데이터가 생성되지 않습니다.

## 저장 방식

- `localStorage`: 현재 입력값을 브라우저에 자동 임시 저장합니다.
- CSV: 계산 입력값과 결과를 한 줄 기록으로 내보내며, 다시 가져와 폼을 복원할 수 있습니다.
- JSON: 앱 상태를 더 안정적으로 백업하고 복원합니다.
