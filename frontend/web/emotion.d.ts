import '@emotion/react';
import { AppTheme } from '@bom-bom/shared';

declare module '@emotion/react' {
  export interface Theme extends AppTheme {}
}
