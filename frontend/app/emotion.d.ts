import '@emotion/react';
import { AppTheme } from 'shared/theme';

declare module '@emotion/react' {
  export interface Theme extends AppTheme {}
}
