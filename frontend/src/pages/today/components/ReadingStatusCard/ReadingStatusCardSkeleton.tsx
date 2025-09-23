import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

function ReadingStatusCardSkeleton() {
  return (
    <Container>
      <TitleWrapper>
        <StatusIcon />
        <SkeletonLine width="70px" height="24px" />
      </TitleWrapper>

      <StreakWrapper>
        <StreakIcon />
        <SkeletonLine width="60px" height="28px" />
        <SkeletonLine width="100px" height="24px" />
        <SkeletonLine width="80px" height="30px" />
      </StreakWrapper>

      <ProgressSection>
        <SkeletonLine width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <SkeletonLine width="60px" height="18px" />
      </ProgressSection>

      <ProgressSection>
        <SkeletonLine width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <SkeletonLine width="60px" height="18px" />
      </ProgressSection>
    </Container>
  );
}

export default ReadingStatusCardSkeleton;

const shimmer = keyframes`
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
`;

const SkeletonBase = styled.div<{ width?: string; height?: string }>`
  width: ${({ width }) => width || '100%'};
  height: ${({ height }) => height || '16px'};
  border-radius: 4px;

  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 37%, #e0e0e0 63%);
  background-size: 400px 100%;

  animation: ${shimmer} 1.4s ease infinite;
`;

const SkeletonLine = SkeletonBase;

const SkeletonBar = styled(SkeletonBase)`
  width: 100%;
  border-radius: 6px;
`;

const Container = styled.section`
  width: 310px;
  padding: 34px 30px;
  border: 1px solid ${({ theme }) => theme.colors.white};
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 15%);

  display: flex;
  gap: 26px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
`;

const TitleWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 10px;
  align-items: center;
`;

const StatusIcon = styled(SkeletonBase)`
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;
`;

const StreakWrapper = styled.div`
  display: flex;
  gap: 10px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const StreakIcon = styled(SkeletonBase)`
  width: 70px;
  height: 70px;
  padding: 18px;
  border-radius: 36px;

  display: flex;
  align-items: center;
  justify-content: center;
`;

const ProgressSection = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;
