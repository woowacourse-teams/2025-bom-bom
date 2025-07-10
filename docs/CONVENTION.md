## 1. Commit

```
type : subject


body (optional)
```

- **type**

```
# Type can be
#   feat    : 새로운 기능 추가  
#   fix     : 수정 (버그, 타입 변경, 로직 수정, ...)
#   hotfix  : 긴급 수정 
#   refactor: 프로덕션 코드 리팩토링 (기능은 그대로)
#   style   : 코드 의미에 영향을 주지 않는 변경사항 (형식 지정, 세미콜론 누락, 네이밍 변경 등)
#   docs    : 문서의 추가, 수정, 삭제
#   test    : 테스트 추가, 수정, 삭제
#   chore   : 기타 변경사항 (빌드 부분 혹은 패키지 매니저 수정사항)
# ------------------
```

- **body**
    - 필요할 경우, head로부터 2줄 아래에 작성

## 2. Pull Request
- 제목: `[{scope}][{issue_key}] {type}:{subject}`
  - `scope`: FE, BE
  - `issue_key`: Jira에서 발급받은 티켓 키
  - `type`: commit type 동일
  - `subject`: 작업 내용 요약
  - ex: `[BE][BOM-5] feat:아티클 목록 조회 기능 추가`

## 3. Branch & Merge Strategy

- 브랜치 이름 : `{type}/{issue_key}`
  - ex: `feat/BOM-5`
- `feature` → `develop` : `Squash & Merge`
- `develop` → `main` : `Create a Commit merge`
