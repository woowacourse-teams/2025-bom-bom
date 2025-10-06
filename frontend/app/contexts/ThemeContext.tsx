import { theme } from '@bom-bom/shared';
import { ThemeProvider } from '@emotion/react';
import React, { PropsWithChildren } from 'react';

export const EmotionThemeProvider = ({ children }: PropsWithChildren) => {
  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};
