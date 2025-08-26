import { Store } from './createStore';
import { ToastData } from './toast.type';
import { toastStore } from './toastStore';

export const getDistributedToasts = (data: ToastData[], limit: number) => {
  const toasts: ToastData[] = [];
  let count = 0;

  data.forEach((item) => {
    count += 1;

    if (count <= limit) {
      toasts.push(item);
    }
  });

  return { toasts };
};

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
    if (toasts.some((storedToast) => storedToast.id === toast.id)) {
      return toasts; // 중복 방지
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

export const toasts = {
  show: showToast,
  hide: hideToast,
  update: updateToast,
  clean: cleanToasts,
} as const;
