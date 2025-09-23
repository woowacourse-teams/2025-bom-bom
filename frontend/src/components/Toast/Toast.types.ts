import type { ReactNode } from 'react';

export type ToastVariant = 'info' | 'success' | 'error';

export type ToastPosition =
  | 'top-left'
  | 'top-right'
  | 'top-center'
  | 'bottom-left'
  | 'bottom-right'
  | 'bottom-center';

export type ToastData = {
  id?: string;
  variant: ToastVariant;
  message: ReactNode;
};
