import { withAndroidManifest, withDangerousMod } from '@expo/config-plugins';
import fs from 'fs';
import path from 'path';

const NETWORK_CONFIG_PATH = 'res/xml/network_security_config.xml';

// 1. network_security_config.xml 파일 생성
const withNetworkSecurityConfigFile = (config) => {
  return withDangerousMod(config, [
    'android',
    (config) => {
      const filePath = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main',
        NETWORK_CONFIG_PATH,
      );

      const dir = path.dirname(filePath);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }

      const xml = `<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">bombom.news</domain>
  </domain-config>
</network-security-config>`;

      fs.writeFileSync(filePath, xml);
      return config;
    },
  ]);
};

/**
 * AndroidManifest.xml 에
 * - usesCleartextTraffic
 * - networkSecurityConfig
 * 속성을 주입하는 Config Plugin
 */
const withCustomAndroidConfig = (config) => {
  return withAndroidManifest(config, (config) => {
    const app = config.modResults.manifest.application[0];

    // 여기서 application 태그에 속성을 주입
    app.$['android:usesCleartextTraffic'] = 'true';
    app.$['android:networkSecurityConfig'] = '@xml/network_security_config';

    return config;
  });
};

export default ({ config }) => {
  return withCustomAndroidConfig(
    withNetworkSecurityConfigFile({
      ...config,
      name: '봄봄',
      slug: 'bombom',
      version: '1.0.0',
      orientation: 'portrait',
      icon: './app/assets/images/logo.png',
      scheme: 'bombom',
      userInterfaceStyle: 'automatic',
      newArchEnabled: true,
      ios: {
        supportsTablet: true,
        infoPlist: {
          NSExceptionDomains: {
            'bombom.news': {
              NSIncludesSubdomains: true,
              NSTemporaryExceptionAllowsInsecureHTTPLoads: true,
              NSTemporaryExceptionMinimumTLSVersion: 'TLSv1.2',
            },
          },
        },
        bundleIdentifier: 'com.antarctica.bombom',
      },
      android: {
        adaptiveIcon: {
          foregroundImage: './app/assets/images/logo-android.png',
          backgroundColor: '#FE5E04',
        },
        edgeToEdgeEnabled: true,
        package: 'com.antarctica.bombom',
      },
      web: {
        bundler: 'metro',
        output: 'static',
        favicon: './app/assets/images/logo.png',
      },
      plugins: [
        'expo-router',
        [
          'expo-splash-screen',
          {
            image: './app/assets/images/logo.png',
            imageWidth: 200,
            resizeMode: 'contain',
            backgroundColor: '#FE5E04',
          },
        ],
      ],

      experiments: {
        typedRoutes: true,
      },

      extra: {
        router: {},
        eas: {
          projectId: 'd2ce3cbd-5c00-4471-8f7f-b4309d071e84',
        },
      },

      owner: 'antarctica-bombom',
    }),
  );
};
