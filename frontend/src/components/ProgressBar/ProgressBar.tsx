import styled from '@emotion/styled';

interface ProgressBarProps {
  rate: number;
}

function ProgressBar({ rate }: ProgressBarProps) {
  return (
    <ProgressOverlay>
      <ProgressGauge rate={rate} />
    </ProgressOverlay>
  );
}

export default ProgressBar;

const ProgressOverlay = styled.div`
  width: 100%;
  height: 10px;
  border-radius: 10px;

  background-color: ${({ theme }) => theme.colors.primaryLight};
`;

const ProgressGauge = styled.div<{ rate: number }>`
  overflow: hidden;

  width: ${({ rate }) => `${rate}%`};
  height: 100%;
  max-width: 100%;
  border-radius: ${({ rate }) => (rate >= 100 ? '10px' : '10px 0 0 10px')};

  background-color: ${({ theme }) => theme.colors.primary};
`;
