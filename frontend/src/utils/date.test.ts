import { formatDate } from './date';

describe('formatDate', () => {
  it('Date 정보를 점(.)으로 구분된 문자열로 변환한다.', () => {
    const date = new Date('2025-07-01T12:00:00Z');
    expect(formatDate(date)).toBe('2025.07.01');
  });
});
