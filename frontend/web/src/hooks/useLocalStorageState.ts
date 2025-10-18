import { useCallback, useEffect, useState, useMemo } from 'react';
import { createStorage } from '@/utils/localStorage';

type Serializable =
  | string
  | number
  | boolean
  | null
  | Serializable[]
  | { [key: string]: Serializable };

export const useLocalStorageState = <T extends Serializable>(
  key: string,
  defaultValue?: T,
) => {
  const storage = useMemo(
    () => createStorage<T>(key, defaultValue),
    [key, defaultValue],
  );
  const [storedValue, setStoredValue] = useState(() => storage.get());

  const setValue = useCallback(
    (value: T | ((prev: T) => T)) => {
      setStoredValue((prev) => {
        const valueToStore = typeof value === 'function' ? value(prev) : value;
        if (valueToStore === null) {
          storage.remove();
        } else {
          storage.set(valueToStore);
        }
        return valueToStore as T;
      });
    },
    [storage],
  );

  useEffect(() => {
    const storageHandler = (event: StorageEvent) => {
      if (event.key === key) setValue(storage.get());
    };

    window.addEventListener('storage', storageHandler);
    return () => {
      window.removeEventListener('storage', storageHandler);
    };
  }, [key, setValue, storage]);

  return [storedValue, setValue] as const;
};
