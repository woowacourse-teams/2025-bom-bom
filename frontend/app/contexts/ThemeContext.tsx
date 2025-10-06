import { ThemeProvider } from '@emotion/react';
import React, { PropsWithChildren } from 'react';
import { theme } from '../styles/theme';

export const EmotionThemeProvider = ({ children }: PropsWithChildren) => {
  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};
