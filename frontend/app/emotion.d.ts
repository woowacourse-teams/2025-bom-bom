import '@emotion/react';
import { AppTheme } from '@bombom/shared/theme';

declare module '@emotion/react' {
  export interface Theme extends AppTheme {}
}
