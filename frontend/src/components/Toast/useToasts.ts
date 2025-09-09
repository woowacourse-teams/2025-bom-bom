import { useSyncExternalStore } from 'react';
import { Store } from './utils/createStore';
import { toastStore } from './utils/toastStore';

export const useToasts = (limit: number, store: Store = toastStore) => {
  const state = useSyncExternalStore(
    store.subscribe,
    () => store.getState(),
    () => store.getState(),
  );
  const toasts = state.slice(0, limit);

  return { toasts };
};
