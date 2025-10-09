import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { theme } from '@bombom/shared/theme';
import LeaderboardItem from './LeaderboardItem';
import { RANKING } from './ReadingKingLeaderboard.constants';
import { getLeaderboardData } from './ReadingKingLeaderboard.utils';
import ReadingKingLeaderboardSkeleton from './ReadingKingLeaderboardSkeleton';
import ReadingKingMyRank from './ReadingKingMyRank';
import { queries } from '@/apis/queries';
import Carousel from '@/components/Carousel/Carousel';
import ArrowIcon from '@/components/icons/ArrowIcon';
import Tooltip from '@/components/Tooltip/Tooltip';
import ReadingKingHelpIcon from '#/assets/svg/help.svg';

const ReadingKingLeaderboard = () => {
  const [rankExplainOpened, setRankExplainOpened] = useState(false);

  const { data: monthlyReadingRank, isLoading } = useQuery(
    queries.monthlyReadingRank({ limit: RANKING.maxRank }),
  );
  const { data: userRank } = useQuery(queries.myMonthlyReadingRank());

  const openRankExplain = () => setRankExplainOpened(true);
  const closeRankExplain = () => setRankExplainOpened(false);

  const monthlyReadingRankContent = monthlyReadingRank ?? [];
  const haveNoContent = !isLoading && monthlyReadingRankContent.length === 0;

  if (!isLoading && haveNoContent) return null;
  if (isLoading) return <ReadingKingLeaderboardSkeleton />;

  return (
    <Container>
      <TitleWrapper>
        <TitleIcon>
          <ArrowIcon width={16} height={16} direction="upRight" />
        </TitleIcon>
        <Title>이달의 독서왕</Title>
        <TooltipButton
          type="button"
          aria-label="이달의 독서왕 랭킹 안내"
          aria-expanded={rankExplainOpened}
          aria-describedby="rank-explain-tooltip"
          onMouseEnter={openRankExplain}
          onMouseLeave={closeRankExplain}
          onFocus={openRankExplain}
          onBlur={closeRankExplain}
        >
          <ReadingKingHelpIcon
            width={20}
            height={20}
            fill={theme.colors.primary}
          />
        </TooltipButton>

        <Tooltip
          id="rank-explain-tooltip"
          opened={rankExplainOpened}
          position="right"
        >
          순위는 10분마다 갱신됩니다.
        </Tooltip>
      </TitleWrapper>

      <Carousel
        autoPlay={false}
        hasSlideButton={true}
        slideButtonPosition="bottom"
      >
        {getLeaderboardData(monthlyReadingRankContent, RANKING.boardUnit).map(
          (leaderboard, leaderboardIndex) => (
            <LeaderboardList key={`leaderboard-${leaderboardIndex}`}>
              {leaderboard.map((item, index) => (
                <LeaderboardItem
                  key={`rank-${index}` + item.nickname}
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

export const Container = styled.div`
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

export const TitleWrapper = styled.div`
  position: relative;
  width: fit-content;

  display: flex;
  gap: 10px;
  align-items: center;
`;

export const TitleIcon = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 12px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

export const Title = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

export const TooltipButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const LeaderboardList = styled.div`
  min-height: fit-content;

  display: flex;
  gap: 32px;
  flex-direction: column;
`;

const Divider = styled.div`
  border-top: 1px solid ${({ theme }) => theme.colors.dividers};
`;
