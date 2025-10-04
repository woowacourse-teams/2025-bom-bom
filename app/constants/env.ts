export const ENV = {
  // Google OAuth Client IDs
  webClientId: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID ?? '',
  iosClientId: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID ?? '',
  androidClientId: process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID ?? '',

  // Web URLs
  localWebUrl: process.env.EXPO_PUBLIC_LOCAL_WEB_URL ?? '',
  devWebUrl: process.env.EXPO_PUBLIC_DEV_WEB_URL ?? '',
  prodWebUrl: process.env.EXPO_PUBLIC_PROD_WEB_URL ?? '',
} as const;
