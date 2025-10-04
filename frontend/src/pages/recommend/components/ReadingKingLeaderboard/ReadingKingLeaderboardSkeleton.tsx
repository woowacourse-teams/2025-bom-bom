import LeaderboardItemSkeleton from './LeaderboardItemSkeleton';
import {
  Container,
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
