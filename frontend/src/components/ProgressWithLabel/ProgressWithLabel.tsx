import styled from '@emotion/styled';
import { ComponentType, SVGProps } from 'react';
import { getProgressInfo } from './ProgressWithLabel.utils';
import { RateFormatType } from './types';
import ProgressBar from '../ProgressBar/ProgressBar';

interface ProgressWithLabelProps {
  label: string;
  Icon: ComponentType<SVGProps<SVGSVGElement>>;
  value: {
    currentCount: number;
    totalCount: number;
  };
  description: string;
  rateFormat?: RateFormatType;
}

function ProgressWithLabel({
  label,
  Icon,
  value: { currentCount, totalCount },
  description,
  rateFormat = 'percentage',
}: ProgressWithLabelProps) {
  const { rate, formattedRate } = getProgressInfo({
    currentCount,
    totalCount,
    rateFormat,
  });

  return (
    <Container>
      <ProgressInfo>
        <StyledIcon as={Icon} />
        <ProgressLabel>{label}</ProgressLabel>
        <ProgressRate>{formattedRate}</ProgressRate>
      </ProgressInfo>
      <ProgressBar rate={rate} />
      <ProgressDescription>{description}</ProgressDescription>
    </Container>
  );
}

export default ProgressWithLabel;

const Container = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;

const ProgressInfo = styled.div`
  width: 100%;

  display: flex;
  gap: 6px;
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

const ProgressRate = styled.span`
  margin-left: auto;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;
