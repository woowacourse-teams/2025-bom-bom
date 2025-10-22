type Serializable =
  | string
  | number
  | boolean
  | null
  | Serializable[]
  | { [key: string]: Serializable };

interface StorageType<T extends Serializable> {
  get: () => T;
  set: (data: T) => void;
  remove: () => void;
}

const storage = window.localStorage;

export const createStorage = <T extends Serializable>(
  key: string,
  defaultData?: T,
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
