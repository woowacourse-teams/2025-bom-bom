import styled from '@emotion/styled';
import ProgressBar from '../ProgressBar';
import { getProgressInfo } from './progress';
import { RateFormatType } from './types';

interface IconProps {
  source: string;
  alternativeText: string;
}

interface Value {
  currentCount: number;
  totalCount: number;
}

interface ProgressWithLabelProps {
  label: string;
  icon: IconProps;
  value: Value;
  description: string;
  rateFormat?: RateFormatType;
}

function ProgressWithLabel({
  label,
  icon: { source, alternativeText },
  value: { currentCount, totalCount },
  description,
  rateFormat = 'percentage',
}: ProgressWithLabelProps) {
  const { progressRate, formattedRate } = getProgressInfo({
    currentCount,
    totalCount,
    rateFormat,
  });

  return (
    <Container>
      <ProgressInfo>
        <Icon src={source} alt={alternativeText} />
        <ProgressLabel>{label}</ProgressLabel>
        <ProgressRate>{formattedRate}</ProgressRate>
      </ProgressInfo>
      <ProgressBar progressRate={progressRate} />
      <ProgressDescription>{description}</ProgressDescription>
    </Container>
  );
}

export default ProgressWithLabel;

const Container = styled.div`
  display: flex;
  flex-direction: column;

  width: 100%;

  gap: 14px;
`;

const ProgressInfo = styled.div`
  display: flex;
  align-items: center;

  width: 100%;

  gap: 6px;
`;

const Icon = styled.img`
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
