import { useCallback, useState } from 'react';

type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

const useLocalStorage = <T extends SerializableType>(
  key: string,
  defaultData: T,
) => {
  const storage = window.localStorage;
  const getInitStorage = () => {
    const initData = storage.getItem(key);
    return initData ? JSON.parse(initData) : defaultData;
  };

  const [data, setData] = useState<T>(getInitStorage);

  const set = useCallback(
    (value: T) => {
      setData(value);
      storage.setItem(key, JSON.stringify(value));
    },
    [storage, key],
  );

  const remove = useCallback(() => {
    setData(defaultData);
    storage.removeItem(key);
  }, [storage, key, defaultData]);

  return {
    data,
    set,
    remove,
  };
};

export default useLocalStorage;
