import { toastStore } from './toastStore';
import type { ToastData } from '../Toast.types';
import type { Store } from './createStore';

export const updateToastsState = (
  store: Store,
  update: (toasts: ToastData[]) => ToastData[],
) => {
  const state = store.getState();
  const toasts = update([...state]);

  store.setState(toasts);
};

export const showToast = (toast: ToastData, store: Store = toastStore) => {
  const id = crypto.randomUUID();

  updateToastsState(store, (toasts) => {
    if (
      toasts.some(
        (storedToast) =>
          storedToast.message === toast.message &&
          storedToast.variant === toast.variant,
      )
    ) {
      return toasts;
    }
    return [...toasts, { ...toast, id }];
  });
};

export const hideToast = (id: string, store: Store = toastStore) => {
  updateToastsState(store, (toasts) =>
    toasts.filter((storedToast) => storedToast.id !== id),
  );
};

export const updateToast = (toast: ToastData, store: Store = toastStore) => {
  updateToastsState(store, (toasts) =>
    toasts.map((storedToast) => {
      if (storedToast.id === toast.id) {
        return { ...storedToast, ...toast };
      }
      return storedToast;
    }),
  );
};

export const cleanToasts = (store: Store = toastStore) => {
  updateToastsState(store, () => []);
};

export const toast = {
  success: (message: string) => showToast({ variant: 'success', message }),
  error: (message: string) => showToast({ variant: 'error', message }),
  info: (message: string) => showToast({ variant: 'info', message }),
} as const;
