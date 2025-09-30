import { Container, RankIconWrapper, UserInfoBox } from './LeaderboardItem';
import SkeletonBox from '@/components/Skeleton/SkeletonBox';

const LeaderboardItemSkeleton = () => (
  <Container>
    <RankIconWrapper>
      <SkeletonBox width="24px" height="24px" />
    </RankIconWrapper>

    <UserInfoBox>
      <SkeletonBox width="80px" height="22px" />
      <SkeletonBox width="60px" height="20px" />
    </UserInfoBox>
  </Container>
);

export default LeaderboardItemSkeleton;
