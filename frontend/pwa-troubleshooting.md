# PWABuilder 문제 해결 가이드

## 🚨 PWABuilder가 manifest.json을 찾지 못하는 경우

### 방법 1: 로컬 IP 주소 사용
- `localhost:3000` 대신 `192.168.1.76:3000` 사용
- PWABuilder에서 IP 주소로 접속 시도

### 방법 2: HTTPS 터널링 사용
```bash
# ngrok 설치 (없는 경우)
brew install ngrok

# 3000 포트를 HTTPS로 터널링
ngrok http 3000
```
- ngrok이 제공하는 HTTPS URL을 PWABuilder에 입력

### 방법 3: 수동으로 manifest.json 업로드
1. PWABuilder에서 "Upload your manifest" 옵션 선택
2. `/frontend/public/manifest.json` 파일 직접 업로드

### 방법 4: JSON 검증 먼저 확인
- https://jsonlint.com/ 에서 manifest.json 유효성 검사
- Chrome 개발자 도구 → Application → Manifest 탭에서 확인

### 방법 5: 간단한 테스트 서버
```bash
# 간단한 HTTP 서버 실행
cd /frontend/public
python3 -m http.server 8000
```
- `localhost:8000`으로 PWABuilder 접속 시도

## 🔍 현재 확인된 상태
✅ manifest.json 파일이 올바른 위치에 있음
✅ HTTP 200 응답으로 정상 접근 가능
✅ JSON 구조가 올바름
❌ PWABuilder가 localhost 인식 못함 (일반적인 문제)

## 💡 권장 해결책
**가장 확실한 방법:** ngrok을 사용해서 HTTPS 터널링하기
```bash
ngrok http 3000
```
이후 제공되는 HTTPS URL을 PWABuilder에 입력