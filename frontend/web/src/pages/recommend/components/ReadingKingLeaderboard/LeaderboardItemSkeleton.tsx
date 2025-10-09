import { Container, RankIconWrapper, UserInfoBox } from './LeaderboardItem';
import Skeleton from '@/components/Skeleton/Skeleton';

const LeaderboardItemSkeleton = () => (
  <Container>
    <RankIconWrapper>
      <Skeleton width="24px" height="24px" />
    </RankIconWrapper>

    <UserInfoBox>
      <Skeleton width="80px" height="22px" />
      <Skeleton width="60px" height="20px" />
    </UserInfoBox>
  </Container>
);

export default LeaderboardItemSkeleton;
