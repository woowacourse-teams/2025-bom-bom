import { accessExternalLink } from './externalLink';

describe('accessExternalLink', () => {
  let mockOpen: jest.SpyInstance;

  beforeEach(() => {
    mockOpen = jest.spyOn(window, 'open').mockImplementation(() => null);
  });

  afterEach(() => {
    mockOpen.mockRestore();
  });

  it('매개변수로 받은 링크를 이용해 새 탭을 연다.', () => {
    const TEST_LINK = 'https://example.com';

    accessExternalLink(TEST_LINK);

    expect(mockOpen).toHaveBeenCalledWith(
      TEST_LINK,
      '_blank',
      'noopener,noreferrer',
    );
  });

  it('함수를 여러 번 호출해도 각각 정상적으로 동작한다.', () => {
    const LINK1 = 'https://example1.com';
    const LINK2 = 'https://example2.com';

    accessExternalLink(LINK1);
    accessExternalLink(LINK2);

    expect(mockOpen).toHaveBeenCalledTimes(2);
    expect(mockOpen).toHaveBeenNthCalledWith(
      1,
      LINK1,
      '_blank',
      'noopener,noreferrer',
    );
    expect(mockOpen).toHaveBeenNthCalledWith(
      2,
      LINK2,
      '_blank',
      'noopener,noreferrer',
    );
  });
});
