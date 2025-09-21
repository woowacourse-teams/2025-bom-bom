import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useCallback } from 'react';
import LeaderboardItem from './LeaderboardItem';
import ReadingKingMyRank from './ReadingKingMyRank';
import { queries } from '@/apis/queries';
import Carousel from '@/components/Carousel/Carousel';
import ArrowIcon from '@/components/icons/ArrowIcon';
import type { ReadingKingRank } from './ReadingKingLeaderboard.types';

const RANKING = {
  maxRank: 10,
  boardUnit: 5,
};

const ReadingKingLeaderboard = () => {
  const { data: monthlyReadingRank, isLoading } = useQuery(
    queries.monthlyReadingRank({ limit: RANKING.maxRank }),
  );
  const { data: userRank } = useQuery(queries.myMonthlyReadingRank());

  const getLeaderboardData = useCallback((data: ReadingKingRank) => {
    const leaderboardLength = Math.ceil(data.length / RANKING.boardUnit);

    const leaderboardData: ReadingKingRank[] = Array.from(
      { length: leaderboardLength },
      () => [],
    );

    data.forEach((rankData, index) => {
      const leaderboardIndex = Math.floor(index / RANKING.boardUnit);
      leaderboardData[leaderboardIndex]?.push(rankData);
    });

    return leaderboardData;
  }, []);

  if (!monthlyReadingRank || monthlyReadingRank.length === 0) {
    return null;
  }

  if (isLoading) {
    return (
      <Container>
        <TitleWrapper>
          <TitleIcon>
            <ArrowIcon width={16} height={16} direction="upRight" />
          </TitleIcon>
          <Title>이달의 독서왕</Title>
        </TitleWrapper>
        <LoadingMessage>데이터를 불러오는 중...</LoadingMessage>
      </Container>
    );
  }

  return (
    <Container>
      <TitleWrapper>
        <TitleIcon>
          <ArrowIcon width={16} height={16} direction="upRight" />
        </TitleIcon>
        <Title>이달의 독서왕</Title>
      </TitleWrapper>

      <Description>순위는 10분마다 변경됩니다.</Description>

      <Carousel showSlideButton={false} animation="none">
        {getLeaderboardData(monthlyReadingRank).map(
          (leaderboard, leaderboardIndex) => (
            <LeaderboardList key={`leaderboard-${leaderboardIndex}`}>
              {leaderboard.map((item, index) => (
                <LeaderboardItem
                  key={`rank-${index}`}
                  rank={item.rank}
                  name={item.nickname}
                  readCount={item.monthlyReadCount}
                />
              ))}
            </LeaderboardList>
          ),
        )}
      </Carousel>

      {userRank && (
        <>
          <Divider />
          <ReadingKingMyRank userRank={userRank} />
        </>
      )}
    </Container>
  );
};

export default ReadingKingLeaderboard;

const Container = styled.div`
  width: 100%;
  max-width: 400px;
  padding: 22px;
  border: 1px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 20px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  display: flex;
  gap: 16px;
  flex-direction: column;

  background: rgb(255 255 255 / 80%);

  backdrop-filter: blur(10px);
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
`;

const TitleIcon = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 12px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const LeaderboardList = styled.div`
  min-height: fit-content;

  display: flex;
  gap: 32px;
  flex-direction: column;
`;

const LoadingMessage = styled.div`
  padding: 40px 20px;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const Divider = styled.div`
  border-top: 1px solid ${({ theme }) => theme.colors.dividers};
`;
