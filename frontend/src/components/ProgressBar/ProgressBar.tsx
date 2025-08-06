import styled from '@emotion/styled';

interface ProgressBarProps {
  rate: number;
  caption?: string;
}

function ProgressBar({ rate, caption }: ProgressBarProps) {
  return (
    <Container>
      <ProgressOverlay>
        <ProgressGauge rate={rate} />
      </ProgressOverlay>
      <ProgressCaption>{caption}</ProgressCaption>
    </Container>
  );
}

export default ProgressBar;

const Container = styled.div`
  width: 100%;
`;

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

  /* 애니메이션 추가 */
  transition: width 0.5s ease-in-out;
`;

const ProgressCaption = styled.p`
  width: 100%;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
  text-align: end;
`;
