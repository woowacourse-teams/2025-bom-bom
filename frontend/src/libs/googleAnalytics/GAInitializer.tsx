import { useEffect } from 'react';
import { initGA } from './initGA';
import { ENV } from '@/apis/env';

const GAInitializer = () => {
  useEffect(() => {
    const isProduction = ENV.nodeEnv === 'production';
    if (!isProduction) return;

    initGA('G-GXQCT7B1GM');
  }, []);
  return null;
};

export default GAInitializer;
