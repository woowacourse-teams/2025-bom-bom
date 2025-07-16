import styled from '@emotion/styled';
import ProgressBar from '../../../components/ProgressBar';
import goalIcon from '../../../../public/assets/goal.svg';

interface ReadingProgressBoxProps {
  label: string;
  rateCaption: string;
  progressRate: number;
  description: string;
}

function ReadingProgressBox({
  label,
  rateCaption,
  progressRate,
  description,
}: ReadingProgressBoxProps) {
  return (
    <Container>
      <ProgressInfo>
        <img src={goalIcon} alt="목표 아이콘" />
        <ProgressLabel>{label}</ProgressLabel>
        <ProgressRate>{rateCaption}</ProgressRate>
      </ProgressInfo>
      <ProgressBar progressRate={progressRate} />
      <ProgressDescription>{description}</ProgressDescription>
    </Container>
  );
}

export default ReadingProgressBox;

const Container = styled.div`
  display: flex;
  flex-direction: column;
  gap: 14px;

  width: 100%;
`;

const ProgressInfo = styled.div`
  display: flex;
  gap: 6px;
`;

const ProgressLabel = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressRate = styled.span`
  margin-left: auto;

  color: ${({ theme }) => theme.colors.textPrimary};
  ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  ${({ theme }) => theme.fonts.caption};
`;
