import { getScrollPercent } from './scroll';

describe('getScrollPercent', () => {
  const originalScrollTop = Object.getOwnPropertyDescriptor(
    document.documentElement,
    'scrollTop',
  );
  const originalScrollHeight = Object.getOwnPropertyDescriptor(
    document.documentElement,
    'scrollHeight',
  );
  const originalClientHeight = Object.getOwnPropertyDescriptor(
    document.documentElement,
    'clientHeight',
  );

  function mockScrollValues({
    scrollTop,
    scrollHeight,
    clientHeight,
  }: {
    scrollTop: number;
    scrollHeight: number;
    clientHeight: number;
  }) {
    Object.defineProperty(document.documentElement, 'scrollTop', {
      configurable: true,
      value: scrollTop,
    });
    Object.defineProperty(document.documentElement, 'scrollHeight', {
      configurable: true,
      value: scrollHeight,
    });
    Object.defineProperty(document.documentElement, 'clientHeight', {
      configurable: true,
      value: clientHeight,
    });
  }

  afterEach(() => {
    if (originalScrollTop) {
      Object.defineProperty(
        document.documentElement,
        'scrollTop',
        originalScrollTop,
      );
    }
    if (originalScrollHeight) {
      Object.defineProperty(
        document.documentElement,
        'scrollHeight',
        originalScrollHeight,
      );
    }
    if (originalClientHeight) {
      Object.defineProperty(
        document.documentElement,
        'clientHeight',
        originalClientHeight,
      );
    }
  });

  it('스크롤 위치가 정상적으로 퍼센트로 계산되는지 확인', () => {
    mockScrollValues({
      scrollTop: 200,
      scrollHeight: 1000,
      clientHeight: 600,
    });

    const percent = getScrollPercent();
    expect(percent).toBeCloseTo((200 / (1000 - 600)) * 100);
  });

  it('scrollHeight와 clientHeight가 같을 때 100% 반환', () => {
    mockScrollValues({
      scrollTop: 0,
      scrollHeight: 800,
      clientHeight: 800,
    });

    const percent = getScrollPercent();
    expect(percent).toBe(100);
  });
});
