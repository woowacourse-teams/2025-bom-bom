import { withAndroidManifest } from '@expo/config-plugins';
import { ConfigContext, ExpoConfig } from 'expo/config';

/**
 * AndroidManifest.xml 수정 플러그인
 * - application 태그에 usesCleartextTraffic, networkSecurityConfig 속성 주입
 */
function withCustomAndroidConfig(config: ConfigContext) {
  // withAndroidManifest: Expo가 AndroidManifest.xml을 파싱한 결과(modResults)를 수정할 수 있게 해주는 래퍼
  // 콜백에서 수정된 config를 반환하면, prebuild 시점에 해당 변경이 반영됨
  return withAndroidManifest(config.config as ExpoConfig, (config) => {
    // AndroidManifest.xml의 최상위 <manifest> 안에 있는 <application> 배열을 가져옴
    // 보통 하나만 존재하므로 [0]으로 접근
    const app = config.modResults.manifest.application?.[0];

    if (!app) {
      return config;
    }

    // Application 태그의 속성 객체(app.$)에 키/값으로 원하는 속성들을 주입
    // 'android:usesCleartextTraffic' = 'true' : HTTP(클리어텍스트) 트래픽을 허용
    app.$['android:usesCleartextTraffic'] = 'true';

    // 수정이 끝난 config를 반환 (필수)
    return config;
  });
}

export default withCustomAndroidConfig;
