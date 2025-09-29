const { withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

// Android 리소스 내에서의 상대 경로(= app/src/main 뒤에 붙일 경로)
// 최종 목적지는 android/app/src/main/res/xml/network_security_config.xml
const NETWORK_CONFIG_PATH = 'res/xml/network_security_config.xml';

/**
 * network_security_config.xml 파일을 생성하는 Config Plugin
 * - prebuild 시점에 android 네이티브 디렉터리로 직접 파일을 써 넣음
 */
function withNetworkSecurityConfigFile(config) {
  // withDangerousMod: 네이티브 디렉터리(예: android/)에 직접 파일을 쓰거나 지우는 작업을 허용
  // 첫 번째 인자 'android'는 Android 플랫폼을 대상으로 한다는 의미
  return withDangerousMod(config, [
    'android',
    (config) => {
      // config.modRequest.platformProjectRoot: 앱의 android 프로젝트 루트 경로 (예: <repo>/android)
      // 그 아래 app/src/main/ + NETWORK_CONFIG_PATH 로 최종 파일 경로를 만든다
      const filePath = path.join(
        config.modRequest.platformProjectRoot, // .../android
        'app/src/main', // .../android/app/src/main
        NETWORK_CONFIG_PATH, // .../android/app/src/main/res/xml/network_security_config.xml
      );

      // 파일이 들어갈 디렉터리 경로(res/xml)를 추출
      const dir = path.dirname(filePath);

      // 대상 디렉터리가 없으면 재귀적으로 생성
      // (CI나 초기 생성 시 xml 폴더가 없을 수 있으므로 안전하게 폴더부터 보장)
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }

      // 실제로 쓸 XML 내용 정의
      // - cleartextTrafficPermitted="true": HTTP 허용
      // - <domain includeSubdomains="true">bombom.news</domain>: 대상 도메인 및 서브도메인 허용
      const xml = `
        <network-security-config>
          <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">bombom.news</domain>
          </domain-config>
        </network-security-config>
      `;

      // 위에서 만든 경로에 XML 내용을 파일로 기록(덮어쓰기)
      // prebuild가 실행될 때마다 최신 상태로 보장됨
      fs.writeFileSync(filePath, xml);

      // 디버깅/확인용 로그 (선택 사항)
      console.log('✅ network_security_config.xml 생성 완료:', filePath);

      // 변경된 config를 반드시 반환
      return config;
    },
  ]);
}

module.exports = withNetworkSecurityConfigFile;
