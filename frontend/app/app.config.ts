import { ConfigContext, ExpoConfig } from 'expo/config';

export default ({ config }: ConfigContext): ExpoConfig => {
  return {
    ...config,
    name: '봄봄',
    slug: 'bombom',
    version: '1.0.3',
    orientation: 'portrait',
    icon: './app/assets/images/logo.png',
    scheme: 'bombom',
    userInterfaceStyle: 'automatic',
    newArchEnabled: true,
    ios: {
      buildNumber: '5',
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
      config: {
        usesNonExemptEncryption: false, // 수출 규정 관련 문서 누락됨 메시지 해결
      },
    },
    android: {
      adaptiveIcon: {
        foregroundImage: './app/assets/images/logo-android.png',
        backgroundColor: '#FE5E04',
      },
      edgeToEdgeEnabled: true,
      package: 'com.antarctica.bombom',
      googleServicesFile: './google-services.json',
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
      [
        '@react-native-google-signin/google-signin',
        {
          iosUrlScheme:
            'com.googleusercontent.apps.190361254930-1464b7md34crhu077urc0hsvtsmb5ks5',
        },
      ],
      [
        'expo-apple-authentication',
        {
          appleTeamId: 'F6XK836QA8',
        },
      ],
      'expo-secure-store',
      'expo-web-browser',
      [
        'expo-web-browser',
        {
          experimentalLauncherActivity: true,
        },
      ],
      [
        'expo-build-properties',
        {
          android: {
            usesCleartextTraffic: true,
          },
        },
      ],
      [
        'expo-notifications',
        {
          icon: './app/assets/images/logo-android.png',
          color: '#FE5E04',
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
  };
};
