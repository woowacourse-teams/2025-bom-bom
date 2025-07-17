import styled from '@emotion/styled';
import BookIcon from '../icons/BookIcon';
import AvatarIcon from '../icons/AvatarIcon';
import TopRightArrowIcon from '../icons/TopRightArrowIcon';

// Mock data for the leaderboard
const leaderboardData = [
  {
    id: 1,
    rank: 1,
    name: '김독서',
    avatar: 'https://via.placeholder.com/35/87CEEB/000000?text=김',
    readCount: 248,
    increment: 15,
    isCrown: true,
    badgeText: '👑 챔피언',
  },
  {
    id: 2,
    rank: 2,
    name: '박뉴스',
    avatar: 'https://via.placeholder.com/35/FFB6C1/000000?text=박',
    readCount: 223,
    increment: 12,
    isCrown: false,
  },
  {
    id: 3,
    rank: 3,
    name: '이정보',
    avatar: 'https://via.placeholder.com/35/DDA0DD/000000?text=이',
    readCount: 201,
    increment: 8,
    isCrown: false,
  },
  {
    id: 4,
    rank: 4,
    name: '최트렌드',
    avatar: 'https://via.placeholder.com/35/98FB98/000000?text=최',
    readCount: 189,
    increment: 6,
    isCrown: false,
  },
  {
    id: 5,
    rank: 5,
    name: '정인사이트',
    avatar: 'https://via.placeholder.com/35/F0E68C/000000?text=정',
    readCount: 167,
    increment: 4,
    isCrown: false,
  },
];

const myRank = {
  rank: 12,
  readCount: 87,
  nextRankDifference: 13,
  progressPercentage: 65,
};

interface LeaderboardItemProps {
  rank: number;
  name: string;
  avatar: string;
  readCount: number;
  increment: number;
  isCrown: boolean;
  badgeText?: string;
}

interface ReadingKingLeaderboardProps {
  data?: LeaderboardItemProps[];
  userRank?: {
    rank: number;
    readCount: number;
    nextRankDifference: number;
    progressPercentage: number;
  };
}

const LeaderboardItem = ({
  rank,
  name,
  avatar,
  readCount,
  increment,
  isCrown,
  badgeText,
}: LeaderboardItemProps) => (
  <ItemContainer>
    <RankIconContainer>
      {isCrown ? (
        <CrownIconWrapper>👑</CrownIconWrapper>
      ) : rank === 2 ? (
        <SecondPlaceIcon>🥈</SecondPlaceIcon>
      ) : rank === 3 ? (
        <ThirdPlaceIcon>🥉</ThirdPlaceIcon>
      ) : (
        <RankNumber>#{rank}</RankNumber>
      )}
    </RankIconContainer>

    <AvatarContainer>
      <AvatarIcon />
    </AvatarContainer>

    <UserInfo>
      <NameContainer>
        <UserName weight={isCrown ? 'normal' : 'medium'}>{name}</UserName>
        {badgeText && <Badge>{badgeText}</Badge>}
      </NameContainer>
      <StatsContainer>
        <ReadCount>{readCount}개 읽음</ReadCount>
        <Increment>+{increment}</Increment>
      </StatsContainer>
    </UserInfo>

    <BookIconContainer>
      <TopRightArrowIcon />
    </BookIconContainer>
  </ItemContainer>
);

export default function ReadingKingLeaderboard({
  data = leaderboardData,
  userRank = myRank,
}: ReadingKingLeaderboardProps) {
  return (
    <Container>
      <Header>
        <TitleContainer>
          <HeaderIcon>
            <TopRightArrowIcon />
          </HeaderIcon>
          <Title>이달의 독서왕</Title>
        </TitleContainer>
      </Header>

      <LeaderboardList>
        {data.map((item) => (
          <LeaderboardItem key={item.id} {...item} />
        ))}
      </LeaderboardList>

      <MyRankSection>
        <MyRankContainer>
          <MyRankBox>
            <MyRankInfo>
              <MyRankLabel>나의 순위</MyRankLabel>
              <MyRankValue>{userRank.rank}위</MyRankValue>
            </MyRankInfo>
            <MyReadInfo>
              <MyReadLabel>읽은 뉴스레터</MyReadLabel>
              <MyReadValue>{userRank.readCount}개</MyReadValue>
            </MyReadInfo>
          </MyRankBox>

          <ProgressSection>
            <ProgressInfo>
              <ProgressLabel>다음 순위까지</ProgressLabel>
              <ProgressValue>
                {userRank.nextRankDifference}개 더 읽기
              </ProgressValue>
            </ProgressInfo>
            <ProgressBar>
              <ProgressFill
                style={{ width: `${userRank.progressPercentage}%` }}
              />
            </ProgressBar>
          </ProgressSection>
        </MyRankContainer>
      </MyRankSection>
    </Container>
  );
}

const Container = styled.div`
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 21px;
  padding: 22px;
  box-shadow:
    0px 10px 15px -3px rgba(0, 0, 0, 0.1),
    0px 4px 6px -4px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(226, 232, 240, 1);
  display: flex;
  flex-direction: column;
  gap: 21px;
  width: 100%;
  max-width: 400px;
`;

const Header = styled.div`
  padding-bottom: 21px;
`;

const TitleContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10.5px;
`;

const HeaderIcon = styled.div`
  width: 28px;
  height: 28px;
  background: #ff9966;
  border-radius: 12.75px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 14px;
    height: 14px;
  }

  path {
    stroke: white;
  }
`;

const Title = styled.h3`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 17.5px;
  font-weight: 400;
  line-height: 24.5px;
  color: #0f172b;
  margin: 0;
`;

const LeaderboardList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 14px;
`;

const ItemContainer = styled.div`
  display: flex;
  align-items: center;
  padding: 10.5px;
  border-radius: 12.75px;
`;

const RankIconContainer = styled.div`
  width: 17.5px;
  height: 17.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10.5px;
`;

const CrownIconWrapper = styled.div`
  font-size: 17.5px;
  line-height: 1;
`;

const SecondPlaceIcon = styled.div`
  font-size: 17.5px;
  line-height: 1;
`;

const ThirdPlaceIcon = styled.div`
  font-size: 17.5px;
  line-height: 1;
`;

const RankNumber = styled.div`
  font-family: 'Inter', sans-serif;
  font-size: 10.76px;
  font-weight: 400;
  color: #62748e;
  line-height: 17.5px;
`;

const AvatarContainer = styled.div`
  margin-right: 10.5px;
`;

const UserInfo = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-bottom: 1px;
`;

const NameContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: -1px;
`;

const UserName = styled.div<{ weight: 'normal' | 'medium' }>`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 14px;
  font-weight: ${({ weight }) => (weight === 'medium' ? '500' : '400')};
  line-height: 21px;
  color: #0f172b;
`;

const Badge = styled.div`
  background: linear-gradient(to right, #fef9c2, #fef3c6);
  color: #a65f00;
  padding: 1.75px 7px;
  border-radius: 6.75px;
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 10.5px;
  font-weight: 500;
  line-height: 14px;
`;

const StatsContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: -1px;
`;

const ReadCount = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 12.3px;
  font-weight: 400;
  color: #45556c;
  line-height: 17.5px;
`;

const Increment = styled.div`
  font-family: 'Inter', sans-serif;
  font-size: 12.1px;
  font-weight: 400;
  color: #ff9966;
  line-height: 17.5px;
`;

const BookIconContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
`;

const MyRankSection = styled.div`
  border-top: 1px solid #f1f5f9;
  padding-top: 36px;
`;

const MyRankContainer = styled.div`
  background: linear-gradient(
    to right,
    rgba(255, 153, 102, 0.1),
    rgba(255, 237, 212, 0.5)
  );
  border-radius: 14px;
  padding: 13px 14px 14px 14px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-direction: column;
  margin-bottom: 10.5px;
`;

const MyRankInfo = styled.div`
  display: flex;
  flex-direction: column;
`;

const MyRankBox = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const MyRankLabel = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 12.3px;
  font-weight: 400;
  color: #45556c;
  line-height: 17.5px;
`;

const MyRankValue = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 21px;
  font-weight: 400;
  color: #0f172b;
  line-height: 28px;
`;

const MyReadInfo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-end;
`;

const MyReadLabel = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 12.3px;
  font-weight: 400;
  color: #45556c;
  line-height: 17.5px;
`;

const MyReadValue = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 21px;
  font-weight: 400;
  color: #ff9966;
  line-height: 28px;
`;

const ProgressSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 3.5px;
`;

const ProgressInfo = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const ProgressLabel = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 10.5px;
  font-weight: 400;
  color: #45556c;
  line-height: 14px;
`;

const ProgressValue = styled.div`
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  font-size: 10.5px;
  font-weight: 400;
  color: #45556c;
  line-height: 14px;
`;

const ProgressBar = styled.div`
  width: 100%;
  height: 7px;
  background: #e2e8f0;
  border-radius: 50px;
  overflow: hidden;
`;

const ProgressFill = styled.div`
  height: 100%;
  background: #ff9966;
  border-radius: 50px;
  transition: width 0.3s ease;
`;
