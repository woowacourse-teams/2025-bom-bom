import '@emotion/react';
import type { AppTheme } from 'shared/theme';

declare module '@emotion/react' {
  export interface Theme extends AppTheme {}
}
