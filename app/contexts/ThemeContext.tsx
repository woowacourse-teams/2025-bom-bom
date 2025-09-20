import React from 'react';
import { ThemeProvider } from '@emotion/react';
import { theme } from '../styles/theme';

interface EmotionThemeProviderProps {
  children: React.ReactNode;
}

export const EmotionThemeProvider: React.FC<EmotionThemeProviderProps> = ({ children }) => {
  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};