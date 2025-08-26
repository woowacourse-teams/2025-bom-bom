import { useSyncExternalStore } from 'react';
import { Store } from './createStore';
import { getDistributedToasts } from './toastActions';
import { toastStore } from './toastStore';

export const useToasts = (limit: number, store: Store = toastStore) => {
  const state = useSyncExternalStore(
    store.subscribe,
    () => store.getState(),
    () => store.getState(),
  );
  const { toasts } = getDistributedToasts(state, limit);

  return { toasts };
};
