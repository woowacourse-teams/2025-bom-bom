import styled from '@emotion/styled';
import { RANKING } from './ReadingKingLeaderboard.constants';
import SkeletonBox from '@/components/Skeleton/SkeletonBox';
import { skeletonStyle } from '@/styles/skeleton';

const ReadingKingLeaderboardSkeleton = () => {
  return (
    <>
      <SkeletonLeaderboardList>
        {Array.from({ length: RANKING.boardUnit }).map((_, index) => (
          <SkeletonLeaderboardItem key={`skeletonLeaderboard-${index}`}>
            <SkeletonRank />
            <UserInfoBox>
              <SkeletonBox width="80px" height="22px" />
              <SkeletonBox width="60px" height="20px" />
            </UserInfoBox>
          </SkeletonLeaderboardItem>
        ))}
      </SkeletonLeaderboardList>
    </>
  );
};

export default ReadingKingLeaderboardSkeleton;

const SkeletonLeaderboardList = styled.div`
  min-height: fit-content;

  display: flex;
  gap: 32px;
  flex-direction: column;
`;

const SkeletonLeaderboardItem = styled.div`
  border-radius: 12px;

  display: flex;
  gap: 10px;
  align-items: center;
`;

const SkeletonRank = styled.div`
  width: 24px;
  height: 24px;
  border-radius: 4px;

  ${skeletonStyle}
`;

const UserInfoBox = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;
