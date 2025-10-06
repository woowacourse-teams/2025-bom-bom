import { useSyncExternalStore } from 'react';
import { toastStore } from './utils/toastStore';
import type { Store } from './utils/createStore';

export const useToasts = (limit: number, store: Store = toastStore) => {
  const state = useSyncExternalStore(
    store.subscribe,
    () => store.getState(),
    () => store.getState(),
  );
  const toasts = state.slice(0, limit);

  return { toasts };
};
