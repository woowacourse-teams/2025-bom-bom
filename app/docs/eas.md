# eas

### eas 환경 세팅

1. eas 로그인

```bash
eas login
```

- 주의사항 : Antatica 조직에 초대가 된 개인 계정으로 로그인해야 함

2. EAS에 설정된 환경변수 가져오기

```bash
eas env:pull --environment development
```

- [공식문서](https://docs.expo.dev/eas/environment-variables/#pull-environment-variables-for-your-local-development)

### eas 빌드

1. 로컬 빌드

```bash
eas build --platform ios --profile production --local
```

2. eas 클라우드 빌드

```bash
eas build --platform ios --profile production
```

### eas 빌드 결과물로 스토어 제출

1. 로컬 빌드 결과물로 스토어 제출

- iOS
  ```bash
  eas submit -p ios —-path ./build-xxx.ipa
  ```

2. eas 클라우드 빌드 결과물로 스토어 제출

```bash
eas submit -p ios —-path ./build-xxx.ipa
```
