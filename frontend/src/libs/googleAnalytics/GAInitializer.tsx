import { useEffect } from 'react';
import { initGA } from './initGA';
import { isProduction } from '@/utils/environment';

const GOOGLE_ANALYTICS_ID = 'G-GXQCT7B1GM';

const GAInitializer = () => {
  useEffect(() => {
    if (!isProduction) return;

    // Google Analytics 초기화
    initGA(GOOGLE_ANALYTICS_ID);
  }, []);
  return null;
};

export default GAInitializer;
