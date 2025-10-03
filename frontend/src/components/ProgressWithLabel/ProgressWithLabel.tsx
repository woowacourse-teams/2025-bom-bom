import styled from '@emotion/styled';
import { getProgressInfo } from './ProgressWithLabel.utils';
import ProgressBar from '../ProgressBar/ProgressBar';
import type { RateFormat } from './types';
import type { ComponentType, SVGProps } from 'react';

interface ProgressWithLabelProps {
  label: string;
  Icon?: ComponentType<SVGProps<SVGSVGElement>>;
  value: {
    currentCount: number;
    totalCount: number;
  };
  description?: string;
  rateFormat?: RateFormat;
  showGraph?: boolean;
}

function ProgressWithLabel({
  label,
  Icon,
  value: { currentCount, totalCount },
  description,
  rateFormat = 'percentage',
  showGraph = true,
}: ProgressWithLabelProps) {
  const { rate, formattedRate } = getProgressInfo({
    currentCount,
    totalCount,
    rateFormat,
  });

  return (
    <Container>
      <ProgressInfo>
        {Icon && <StyledIcon as={Icon} />}
        <ProgressLabel>{label}</ProgressLabel>
        <ProgressRate>{formattedRate}</ProgressRate>
      </ProgressInfo>
      {showGraph && <ProgressBar rate={rate} />}
      {description && <ProgressDescription>{description}</ProgressDescription>}
    </Container>
  );
}

export default ProgressWithLabel;

export const Container = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;

export const ProgressInfo = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  align-items: center;
`;

const StyledIcon = styled.img`
  width: 16px;
  height: 16px;

  object-fit: cover;
  object-position: center;
`;

const ProgressLabel = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

export const ProgressRate = styled.span`
  margin-left: auto;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;
