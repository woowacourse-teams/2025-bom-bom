import { useEffect } from 'react';
import { initGA } from './initGA';
import { ENV } from '@/apis/env';

const GAInitializer = () => {
  useEffect(() => {
    const isProduction = ENV.nodeEnv === 'production';
    if (!isProduction) return;

    if (!ENV.googleAnalyticsId) {
      console.warn('[GA] googleAnalyticsId is missing in production');
      return;
    }

    initGA(ENV.googleAnalyticsId);
  }, []);
  return null;
};

export default GAInitializer;
