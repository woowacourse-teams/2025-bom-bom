const IOS_URL_SCHEME = process.env.EXPO_PUBLIC_IOS_URL_SCHEME ?? '';

export default {
  expo: {
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
        NSAppTransportSecurity: {
          NSExceptionDomains: {
            'bombom.news': {
              NSIncludesSubdomains: true,
              NSTemporaryExceptionAllowsInsecureHTTPLoads: true,
              NSTemporaryExceptionMinimumTLSVersion: 'TLSv1.2',
            },
          },
        },
      },
      bundleIdentifier: 'com.antarctica.bombom.app', // 서버와 논의 후 .app은 제거 예정
      usesAppleSignIn: true,
    },
    android: {
      adaptiveIcon: {
        foregroundImage: './app/assets/images/logo.png',
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
      [
        '@react-native-google-signin/google-signin',
        {
          iosUrlScheme: IOS_URL_SCHEME,
        },
      ],
      [
        'expo-apple-authentication',
        {
          appleTeamId: 'F6XK836QA8',
        },
      ],
    ],
    experiments: {
      typedRoutes: true,
    },
    extra: {
      router: {},
      eas: {
        projectId: '028392dc-6a98-48a9-8874-30f193313eda',
      },
    },
  },
};
