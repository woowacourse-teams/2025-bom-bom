const { withAndroidManifest } = require('@expo/config-plugins');

/**
 * AndroidManifest.xml 수정 플러그인
 * - application 태그에 usesCleartextTraffic, networkSecurityConfig 속성 주입
 */
function withCustomAndroidConfig(config) {
  return withAndroidManifest(config, (config) => {
    const app = config.modResults.manifest.application[0];

    // Application 태그에 속성 주입
    app.$['android:usesCleartextTraffic'] = 'true';
    app.$['android:networkSecurityConfig'] = '@xml/network_security_config';

    return config;
  });
}

module.exports = withCustomAndroidConfig;
