import { useCallback, useSyncExternalStore } from 'react';

type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

const storage = window.localStorage;

const useLocalStorage = <T extends SerializableType>(
  key: string,
  defaultData: T,
) => {
  const getSnapshot = () => {
    return (storage.getItem(key) ?? defaultData) as T;
  };

  const getServerSnapshot = () => {
    return null;
  };

  const subscribe = useCallback((listener: () => void) => {
    window.addEventListener('storage', listener);
    return () => window.removeEventListener('storage', listener);
  }, []);

  const store = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const set = useCallback(
    (value: T) => {
      const newValue = JSON.stringify(value);
      window.localStorage.setItem(key, newValue);
      window.dispatchEvent(new StorageEvent('storage', { key, newValue }));
    },
    [key],
  );

  const remove = useCallback(() => {
    storage.removeItem(key);
  }, [key]);

  return {
    data: store,
    set,
    remove,
  };
};

export default useLocalStorage;
