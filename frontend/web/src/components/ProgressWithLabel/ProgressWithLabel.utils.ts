import { calculateRate } from '@/utils/math';
import type { RateFormat } from './types';

interface FormatRateParams {
  currentCount: number;
  totalCount: number;
  rateFormat: RateFormat;
}

const format = {
  percentage: (current: number, total: number) =>
    `${calculateRate(current, total)}%`,
  ratio: (current: number, total: number) => `${current}/${total}`,
};

export const getProgressInfo = ({
  currentCount,
  totalCount,
  rateFormat,
}: FormatRateParams) => {
  const rate = calculateRate(currentCount, totalCount);
  const formattedRate = format[rateFormat](currentCount, totalCount);

  return {
    rate,
    formattedRate,
  };
};
