export const calculateRate = (value: number, total: number) => {
  if (total === 0) {
    return 0;
  }

  return Math.round((value / total) * 100);
};
