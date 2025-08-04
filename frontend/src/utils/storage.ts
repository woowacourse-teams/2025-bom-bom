type SerializableType =
  | string
  | number
  | boolean
  | null
  | SerializableType[]
  | { [key: string]: SerializableType };

interface StorageType<T extends SerializableType> {
  localStorage: Storage;
  get: () => T;
  set: (data: T) => void;
}

export const createStorage = <T extends SerializableType>(
  key: string,
  defaultData: T,
): StorageType<T> => ({
  localStorage: window.localStorage,

  get() {
    const data = this.localStorage.getItem(key);
    return data ? JSON.parse(data) : defaultData;
  },

  set(data) {
    const stringifyData = JSON.stringify(data);
    this.localStorage.setItem(key, stringifyData);
  },
});
