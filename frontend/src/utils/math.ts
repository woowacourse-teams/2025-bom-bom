export const calculateRate = (value: number, total: number) => {
  if (total === 0) {
    throw new Error('나누어지는 수(total)은 0이 될 수 없습니다.');
  }

  return Math.round((value / total) * 100);
};
