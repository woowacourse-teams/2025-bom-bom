import { theme } from 'shared/theme';
import { ThemeProvider } from '@emotion/react';
import { PropsWithChildren } from 'react';

export const EmotionThemeProvider = ({ children }: PropsWithChildren) => {
  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};
