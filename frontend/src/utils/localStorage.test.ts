import { createStorage } from './localStorage';

describe('createStorage', () => {
  const KEY = 'test';

  beforeEach(() => {
    window.localStorage.clear();
    jest.spyOn(Storage.prototype, 'getItem');
    jest.spyOn(Storage.prototype, 'setItem');
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('local storage에 key가 없으면 defaultData를 반환한다.', () => {
    const DEFAULT_DATA = 'data';

    const storage = createStorage<string>(KEY, DEFAULT_DATA);
    const result = storage.get();

    expect(window.localStorage.getItem).toHaveBeenCalledWith(KEY);
    expect(result).toEqual(DEFAULT_DATA);
  });

  it('local storage에 key 값이 존재하면 해당 데이터를 파싱하여 반환한다.', () => {
    const DEFAULT_DATA = { count: 0 };
    const STORED_DATA = { count: 42 };

    window.localStorage.setItem(KEY, JSON.stringify(STORED_DATA));
    const storage = createStorage<{ count: number }>(KEY, DEFAULT_DATA);

    expect(storage.get()).toEqual(STORED_DATA);
  });

  it('local storage에 데이터가 정상적으로 저장된다.', () => {
    const DEFAULT_DATA = { count: 0 };
    const NEW_DATA = { count: 42 };
    const storage = createStorage<{ count: number }>(KEY, DEFAULT_DATA);

    storage.set(NEW_DATA);

    expect(window.localStorage.setItem).toHaveBeenCalledWith(
      KEY,
      JSON.stringify(NEW_DATA),
    );
    expect(storage.get()).toEqual(NEW_DATA);
  });
});
