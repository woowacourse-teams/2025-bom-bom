import { GOOGLE_ANALYTICS_ID } from './constants';
import { logger } from '@/utils/logger';

declare global {
  interface Window {
    dataLayer: unknown[];
    gtag?: (...args: unknown[]) => void;
  }
}

export const initGA = (
  googleAnalyticsId: string,
  gtagUrl: string = 'https://www.googletagmanager.com/gtag/js',
) => {
  if (!googleAnalyticsId) {
    logger.warn('[GA] Measurement ID missing');
    return;
  }

  // gtag.js 삽입
  const script = document.createElement('script');
  script.async = true;
  script.src = `${gtagUrl}?id=${googleAnalyticsId}`;
  document.head.appendChild(script);

  // gtag 초기화
  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag() {
    // eslint-disable-next-line prefer-rest-params
    window.dataLayer.push(arguments);
  };

  window.gtag('js', new Date());
  window.gtag('config', GOOGLE_ANALYTICS_ID, {
    send_page_view: false,
  });
};
