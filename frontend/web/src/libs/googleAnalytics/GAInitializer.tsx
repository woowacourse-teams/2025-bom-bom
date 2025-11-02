import { useEffect } from 'react';
import { DEV_GOOGLE_ANALYTICS_ID, GOOGLE_ANALYTICS_ID } from './constants';
import { initGA } from './initGA';
import { isDevelopment, isProduction } from '@/utils/environment';

const GAInitializer = () => {
  useEffect(() => {
    if (isDevelopment) initGA(DEV_GOOGLE_ANALYTICS_ID);
    if (isProduction) initGA(GOOGLE_ANALYTICS_ID);
  }, []);
  return null;
};

export default GAInitializer;
