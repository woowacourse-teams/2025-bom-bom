import LeaderboardItemSkeleton from './LeaderboardItemSkeleton';
import {
  Container,
  Description,
  LeaderboardList,
  Title,
  TitleIcon,
  TitleWrapper,
} from './ReadingKingLeaderboard';
import { RANKING } from './ReadingKingLeaderboard.constants';
import ArrowIcon from '@/components/icons/ArrowIcon';

const ReadingKingLeaderboardSkeleton = () => {
  return (
    <>
      <Container>
        <TitleWrapper>
          <TitleIcon>
            <ArrowIcon width={16} height={16} direction="upRight" />
          </TitleIcon>
          <Title>이달의 독서왕</Title>
        </TitleWrapper>

        <Description>순위는 10분마다 변경됩니다.</Description>

        <LeaderboardList>
          {Array.from({ length: RANKING.boardUnit }).map((_, index) => (
            <LeaderboardItemSkeleton key={`skeletonLeaderboard-${index}`} />
          ))}
        </LeaderboardList>
      </Container>
    </>
  );
};

export default ReadingKingLeaderboardSkeleton;
