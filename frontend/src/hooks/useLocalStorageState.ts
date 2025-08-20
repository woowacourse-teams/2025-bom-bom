import { useCallback, useEffect, useState, useMemo } from 'react';
import { createStorage } from '@/utils/localStorage';

type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

export const useLocalStorageState = <
  T extends SerializableType,
  K extends string,
>(
  key: K,
  defaultValue?: T,
) => {
  const storage = useMemo(
    () => createStorage<T, K>(key, defaultValue),
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
