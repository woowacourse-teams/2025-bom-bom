import { useEffect } from 'react';
import ReactGA from 'react-ga4';
import { ENV } from '@/apis/env';

const GAInitializer = () => {
  useEffect(() => {
    const isProduction = ENV.nodeEnv === 'production';
    if (!isProduction) return;

    if (!ENV.googleAnalyticsId) {
      console.warn('[GA] googleAnalyticsId is missing in production');
      return;
    }

    ReactGA.initialize(ENV.googleAnalyticsId);
  }, []);
  return null;
};

export default GAInitializer;
