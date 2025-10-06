import { useEffect } from 'react';
import { GOOGLE_ANALYTICS_ID } from './constants';
import { initGA } from './initGA';
import { isProduction } from '@/utils/environment';

const GAInitializer = () => {
  useEffect(() => {
    if (!isProduction) return;

    // Google Analytics 초기화
    initGA(GOOGLE_ANALYTICS_ID);
  }, []);
  return null;
};

export default GAInitializer;
