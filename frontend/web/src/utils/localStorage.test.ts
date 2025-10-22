import { createStorage } from './localStorage';

describe('createStorage', () => {
  const KEY = 'test';
  const DEFAULT_DATA = { count: 0 };
  let storage: ReturnType<typeof createStorage<{ count: number }>>;

  beforeEach(() => {
    window.localStorage.clear();
    jest.spyOn(Storage.prototype, 'getItem');
    jest.spyOn(Storage.prototype, 'setItem');
    jest.spyOn(Storage.prototype, 'removeItem');

    storage = createStorage(KEY, DEFAULT_DATA);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('local storage에 key 값이 없으면 defaultData를 반환한다.', () => {
    const result = storage.get();

    expect(window.localStorage.getItem).toHaveBeenCalledWith(KEY);
    expect(result).toEqual(DEFAULT_DATA);
  });

  it('get: local storage에 key 값이 존재하면 해당 데이터를 파싱하여 반환한다.', () => {
    const STORED_DATA = { count: 42 };
    window.localStorage.setItem(KEY, JSON.stringify(STORED_DATA));

    expect(storage.get()).toEqual(STORED_DATA);
  });

  it('set: local storage에 데이터가 정상적으로 저장된다.', () => {
    const NEW_DATA = { count: 42 };
    storage.set(NEW_DATA);

    expect(window.localStorage.setItem).toHaveBeenCalledWith(
      KEY,
      JSON.stringify(NEW_DATA),
    );
    expect(storage.get()).toEqual(NEW_DATA);
  });

  it('remove: local storage의 데이터가 정상적으로 제거된다.', () => {
    const STORED_DATA = { count: 42 };
    window.localStorage.setItem(KEY, JSON.stringify(STORED_DATA));
    storage.remove();

    expect(window.localStorage.removeItem).toHaveBeenCalledWith(KEY);
    expect(window.localStorage.getItem(KEY)).toBeNull();
  });
});
