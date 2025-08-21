import { useEffect } from 'react';
import { initGA } from './initGA';
import { ENV } from '@/apis/env';

const GOOGLE_ANALYTICS_ID = 'G-GXQCT7B1GM';

const GAInitializer = () => {
  useEffect(() => {
    const isProduction = ENV.nodeEnv === 'production';
    if (!isProduction) return;

    // Google Analytics 초기화
    initGA(GOOGLE_ANALYTICS_ID);
  }, []);
  return null;
};

export default GAInitializer;
