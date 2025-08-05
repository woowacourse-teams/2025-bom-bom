type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

interface StorageType<T extends SerializableType> {
  get: () => T;
  set: (data: T) => void;
  remove: () => void;
}

const storage = window.localStorage;

export const createStorage = <T extends SerializableType, K extends string>(
  key: K,
  defaultData: T,
): StorageType<T> => ({
  get() {
    const data = storage.getItem(key);
    return data ? JSON.parse(data) : defaultData;
  },

  set(data) {
    const stringifyData = JSON.stringify(data);
    storage.setItem(key, stringifyData);
  },

  remove() {
    storage.removeItem(key);
  },
});
