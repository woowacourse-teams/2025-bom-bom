import { showToast } from './utils/toastActions';

export const toast = {
  success: (message: string) => showToast({ type: 'success', message }),
  error: (message: string) => showToast({ type: 'error', message }),
  info: (message: string) => showToast({ type: 'info', message }),
} as const;
