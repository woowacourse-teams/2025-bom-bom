export const Env = {
  // Google OAuth Client IDs
  WEB_CLIENT_ID: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID ?? '',
  IOS_CLIENT_ID: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID ?? '',
  ANDROID_CLIENT_ID: process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID ?? '',

  // URL Schemes
  IOS_URL_SCHEME: process.env.EXPO_PUBLIC_IOS_URL_SCHEME ?? '',

  // Web URLs
  DEV_WEB_URL: process.env.EXPO_PUBLIC_DEV_WEB_URL ?? '',
  PROD_WEB_URL: process.env.EXPO_PUBLIC_PROD_WEB_URL ?? '',
} as const;
