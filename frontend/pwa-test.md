# PWA 설정 테스트 가이드

## 설치된 PWA 기능 확인하기

### 1. 브라우저에서 확인하기

1. **Chrome에서 개발자 도구 열기** (`F12` 또는 우클릭 → 검사)
2. **Application 탭** 클릭
3. 다음 항목들이 올바르게 설정되었는지 확인:
   - **Manifest**: `/manifest.json` 파일 내용 확인
   - **Service Workers**: `/sw.js` 등록 상태 확인
   - **Storage**: 캐시 저장소 확인

### 2. PWA 설치 가능 여부 확인

1. **Chrome 주소창에서** "앱 설치" 아이콘 (⊞) 찾기
2. 또는 **Chrome 메뉴** → "앱 설치" 옵션 확인
3. **모바일에서** "홈 화면에 추가" 옵션 확인

### 3. Lighthouse PWA 점수 확인

1. **개발자 도구** → **Lighthouse 탭**
2. **Progressive Web App** 체크 후 **Generate report** 클릭
3. PWA 점수와 개선 사항 확인

### 4. 오프라인 작동 확인

1. **개발자 도구** → **Network 탭**
2. **Offline** 체크박스 활성화
3. 페이지 새로고침하여 오프라인 상태에서 작동 확인

## 설정된 PWA 기능

### ✅ Web App Manifest

- 앱 이름: "봄봄 - 뉴스레터 읽기 플랫폼"
- 짧은 이름: "봄봄"
- 테마 색상: #FF9966
- 배경 색상: #ffffff
- 표시 모드: standalone
- 시작 URL: /
- 다양한 크기 아이콘 (96x96, 144x144, 192x192, 512x512)

### ✅ Service Worker

- 정적 자원 캐싱 (Cache First 전략)
- API 요청 (Network First 전략)
- 오프라인 지원
- 푸시 알림 준비 (향후 확장 가능)

### ✅ PWA 메타태그

- viewport 설정
- theme-color
- apple-mobile-web-app-\* 설정
- msapplication-\* 설정

### ✅ 앱 바로가기 (Shortcuts)

- 오늘의 아티클 (/)
- 보관함 (/storage)
- 추천 (/recommend)

## 다음 단계 (선택사항)

### 1. 푸시 알림 구현

- 사용자 알림 권한 요청
- 서버에서 푸시 메시지 전송 기능

### 2. 고급 캐싱 전략

- Workbox 라이브러리 사용
- 더 정교한 캐시 관리

### 3. 앱 업데이트 알림

- Service Worker 업데이트 감지
- 사용자에게 새 버전 알림

### 4. PWA 성능 최적화

- 중요한 자원 우선 캐싱
- 백그라운드 동기화

---

현재 기본적인 PWA 기능이 모두 설치되었으므로, 사용자는 브라우저에서 "앱으로 설치"할 수 있습니다!
