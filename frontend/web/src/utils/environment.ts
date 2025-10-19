import { ENV } from '@/apis/env';

export const isServer = typeof window === 'undefined';

const hostname = !isServer ? window.location.hostname : '';

export const isProduction =
  ENV.nodeEnv === 'production' && hostname === 'www.bombom.news';
export const isDevelopment =
  ENV.nodeEnv === 'production' && hostname.includes('dev');
export const isLocal =
  ENV.nodeEnv === 'development' || hostname === 'localhost';
