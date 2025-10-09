export const ENV = {
  // Google OAuth Client IDs
  webClientId: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID ?? '',
  iosClientId: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID ?? '',

  // Web URLs
  webUrl: process.env.EXPO_PUBLIC_WEB_URL ?? '',
} as const;
