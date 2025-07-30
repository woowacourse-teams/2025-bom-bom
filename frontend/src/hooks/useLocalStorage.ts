import { useCallback, useState } from 'react';

type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

const ROOT_KEY = 'bombom';

const useLocalStorage = <T extends SerializableType>(
  key: string,
  defaultData: T,
) => {
  const storage = window.localStorage;
  const getInitStorage = useCallback((): Record<string, string> => {
    return JSON.parse(storage.getItem(ROOT_KEY) ?? '{}');
  }, [storage]);

  const [data, setData] =
    useState<Record<string, SerializableType>>(getInitStorage);

  const get = useCallback(() => {
    return (data[key] ?? defaultData) as T;
  }, [data, key, defaultData]);

  const set = useCallback(
    (value: T) => {
      setData((prev) => {
        const newData = {
          ...prev,
          [key]: value,
        };

        storage.setItem(ROOT_KEY, JSON.stringify(newData));
        return newData;
      });
    },
    [storage, key],
  );

  const remove = useCallback(() => {
    setData((prev) => {
      const newData = Object.fromEntries(
        Object.entries(prev).filter(([targetKey]) => targetKey !== key),
      );

      storage.setItem(ROOT_KEY, JSON.stringify(newData));
      return newData;
    });
  }, [storage, key]);

  return {
    get,
    set,
    remove,
  };
};

export default useLocalStorage;
