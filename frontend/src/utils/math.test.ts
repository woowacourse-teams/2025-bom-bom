import { calculateRate } from './math';

describe('calculateRate', () => {
  it('인자로 받은 두 수의 백분율을 계산한다.', () => {
    const TOTAL = 200;
    const VALUE = 50;
    expect(calculateRate(VALUE, TOTAL)).toBe(25);
  });

  it('전체를 의미하는 total의 값이 0일 때 에러를 던진다.', () => {
    const TOTAL = 0;
    const VALUE = 50;
    expect(() => calculateRate(VALUE, TOTAL)).toThrow(
      '나누어지는 수(total)은 0이 될 수 없습니다.',
    );
  });
});
