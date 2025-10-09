import type { ToastData } from '../Toast.types';

type StoreSubscriber = (state: ToastData[]) => void;

export interface Store {
  getState: () => ToastData[];
  setState: (value: ToastData[] | ((prev: ToastData[]) => ToastData[])) => void;
  subscribe: (callback: StoreSubscriber) => () => void;
}

export const createStore = (initialState: ToastData[]): Store => {
  let state = initialState;
  const listeners = new Set<StoreSubscriber>();

  return {
    getState: () => state,
    setState: (value) => {
      state = typeof value === 'function' ? value(state) : value;
      listeners.forEach((listener) => listener(state));
    },
    subscribe: (callback) => {
      listeners.add(callback);
      return () => listeners.delete(callback);
    },
  };
};
