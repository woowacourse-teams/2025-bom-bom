import styled from '@emotion/styled';
import ProgressBar from '@/components/ProgressBar/ProgressBar';
import { calculateRate } from '@/utils/math';
import type { components } from '@/types/openapi';

interface ReadingKingMyRankProps {
  userRank: components['schemas']['MemberMonthlyReadingRankResponse'];
}

const ReadingKingMyRank = ({ userRank }: ReadingKingMyRankProps) => {
  const progressRate = calculateRate(
    userRank.readCount,
    userRank.readCount + userRank.nextRankDifference,
  );

  const rankSummary = `현재 나의 순위 ${userRank.rank}위. 읽은 뉴스레터 ${userRank.readCount}개. 다음 순위까지 ${userRank.nextRankDifference}개 더 읽기. 진행률 ${progressRate}%`;

  return (
    <Container aria-label={rankSummary} tabIndex={0}>
      <MyRankInfo>
        <InfoWrapper>
          <MyRankLabel>나의 순위</MyRankLabel>
          <MyRankLabel>읽은 뉴스레터</MyRankLabel>
        </InfoWrapper>
        <InfoWrapper>
          <MyRankValue>{userRank.rank}위</MyRankValue>
          <MyReadValue>{userRank.readCount}개</MyReadValue>
        </InfoWrapper>
      </MyRankInfo>

      <ProgressBox>
        <InfoWrapper>
          <ProgressLabel>다음 순위까지</ProgressLabel>
          <ProgressLabel>{userRank.nextRankDifference}개 더 읽기</ProgressLabel>
        </InfoWrapper>
        <ProgressBar rate={progressRate} />
      </ProgressBox>
    </Container>
  );
};

export default ReadingKingMyRank;

const Container = styled.section`
  padding: 16px;
  border-radius: 16px;

  display: flex;
  gap: 12px;
  flex-direction: column;

  background-color: ${({ theme }) => `${theme.colors.primary}10`};
`;

const MyRankInfo = styled.div`
  display: flex;
  flex-direction: column;
`;

const InfoWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const MyRankLabel = styled.div`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const MyRankValue = styled.div`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const MyReadValue = styled.div`
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const ProgressBox = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;

const ProgressLabel = styled.div`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;
