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

const getStorage = () => {
  if (typeof window === 'undefined') {
    // SSR 환경에서는 더미 storage 반환
    return {
      getItem: () => null,
      setItem: () => {},
      removeItem: () => {},
    };
  }
  return window.localStorage;
};

export const createStorage = <T extends Serializable, K extends string>(
  key: K,
  defaultData?: T,
): StorageType<T> => ({
  get() {
    const storage = getStorage();
    const data = storage.getItem(key);
    return data ? JSON.parse(data) : defaultData;
  },

  set(data) {
    const storage = getStorage();
    const stringifyData = JSON.stringify(data);
    storage.setItem(key, stringifyData);
  },

  remove() {
    const storage = getStorage();
    storage.removeItem(key);
  },
});
