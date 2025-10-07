import '@emotion/react';
import { AppTheme } from 'shared';

declare module '@emotion/react' {
  export interface Theme extends AppTheme {}
}
