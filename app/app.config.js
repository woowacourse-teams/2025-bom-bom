const withNetworkSecurityConfigFile = require('./plugins/withNetworkSecurityConfigFile');
const withCustomAndroidConfig = require('./plugins/withCustomAndroidConfig');

module.exports = ({ config }) => {
  return withCustomAndroidConfig(
    withNetworkSecurityConfigFile({
      ...config,
      name: '봄봄',
      slug: 'bombom',
      version: '1.0.1',
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
