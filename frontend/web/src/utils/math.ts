export const calculateRate = (value: number, total: number) => {
  if (total === 0) {
    return 0;
  }

  return Math.round((value / total) * 100);
};

export const calculateAngle = (
  x1: number,
  y1: number,
  x2: number,
  y2: number,
) => {
  const radius = Math.atan2(y2 - y1, x2 - x1);
  return (radius * 180) / Math.PI;
};
