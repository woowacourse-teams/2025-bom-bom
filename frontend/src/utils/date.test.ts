import { formatDateToDotString } from './date';

describe('formatDateToDotString', () => {
  it('Date 정보를 점(.)으로 구분된 문자열로 변환한다.', () => {
    const date = new Date('2025-07-01T12:00:00Z');
    expect(formatDateToDotString(date)).toBe('2025.07.01');
  });
});
