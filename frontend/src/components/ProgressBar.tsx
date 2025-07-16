import styled from '@emotion/styled';

interface ProgressBarProps {
  progressRate: number;
}

function ProgressBar({ progressRate }: ProgressBarProps) {
  return (
    <ProgressOverlay>
      <ProgressGauge progressRate={progressRate} />
    </ProgressOverlay>
  );
}

export default ProgressBar;

const ProgressOverlay = styled.div`
  width: 100%;
  height: 10px;

  background-color: ${({ theme }) => theme.colors.primaryLight};
  border-radius: 10px;
`;

const ProgressGauge = styled.div<{ progressRate: number }>`
  overflow: hidden;

  width: ${({ progressRate }) => `${progressRate}%`};
  height: 100%;

  background-color: ${({ theme }) => theme.colors.primary};
  border-radius: ${({ progressRate }) =>
    progressRate === 100 ? '10px' : '10px 0 0 10px'};
`;
