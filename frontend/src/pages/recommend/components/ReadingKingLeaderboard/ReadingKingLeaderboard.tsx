import styled from '@emotion/styled';
import AvatarIcon from '@/components/icons/AvatarIcon';
import TopRightArrowIcon from '@/components/icons/TopRightArrowIcon';

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
          <LeaderboardItem key={item.rank} {...item} />
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
  display: flex;
  gap: 21px;
  flex-direction: column;

  width: 100%;
  max-width: 400px;
  padding: 22px;
  border: 1px solid rgb(226 232 240 / 100%);
  border-radius: 21px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  background: rgb(255 255 255 / 80%);

  backdrop-filter: blur(10px);
`;

const Header = styled.div`
  padding-bottom: 21px;
`;

const TitleContainer = styled.div`
  display: flex;
  gap: 10.5px;
  align-items: center;
`;

const HeaderIcon = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;
  border-radius: 12.75px;

  background: #f96;

  svg {
    width: 14px;
    height: 14px;
  }

  path {
    stroke: white;
  }
`;

const Title = styled.h3`
  margin: 0;

  color: #0f172b;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 17.5px;
  line-height: 24.5px;
`;

const LeaderboardList = styled.div`
  display: flex;
  gap: 14px;
  flex-direction: column;
`;

const ItemContainer = styled.div`
  display: flex;
  align-items: center;

  padding: 10.5px;
  border-radius: 12.75px;
`;

const RankIconContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 17.5px;
  height: 17.5px;
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
  color: #62748e;
  font-family: Inter, sans-serif;
  font-weight: 400;
  font-size: 10.76px;
  line-height: 17.5px;
`;

const AvatarContainer = styled.div`
  margin-right: 10.5px;
`;

const UserInfo = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;

  padding-bottom: 1px;
`;

const NameContainer = styled.div`
  display: flex;
  gap: 7px;
  align-items: center;

  margin-bottom: -1px;
`;

const UserName = styled.div<{ weight: 'normal' | 'medium' }>`
  color: #0f172b;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: ${({ weight }) => (weight === 'medium' ? '500' : '400')};
  font-size: 14px;
  line-height: 21px;
`;

const Badge = styled.div`
  padding: 1.75px 7px;
  border-radius: 6.75px;

  background: linear-gradient(to right, #fef9c2, #fef3c6);

  color: #a65f00;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 500;
  font-size: 10.5px;
  line-height: 14px;
`;

const StatsContainer = styled.div`
  display: flex;
  gap: 7px;
  align-items: center;

  margin-bottom: -1px;
`;

const ReadCount = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 12.3px;
  line-height: 17.5px;
`;

const Increment = styled.div`
  color: #f96;
  font-family: Inter, sans-serif;
  font-weight: 400;
  font-size: 12.1px;
  line-height: 17.5px;
`;

const BookIconContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
`;

const MyRankSection = styled.div`
  padding-top: 36px;
  border-top: 1px solid #f1f5f9;
`;

const MyRankContainer = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  justify-content: space-between;

  margin-bottom: 10.5px;
  padding: 13px 14px 14px;
  border-radius: 14px;

  background: linear-gradient(
    to right,
    rgb(255 153 102 / 10%),
    rgb(255 237 212 / 50%)
  );
`;

const MyRankInfo = styled.div`
  display: flex;
  flex-direction: column;
`;

const MyRankBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const MyRankLabel = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 12.3px;
  line-height: 17.5px;
`;

const MyRankValue = styled.div`
  color: #0f172b;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 21px;
  line-height: 28px;
`;

const MyReadInfo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-end;
`;

const MyReadLabel = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 12.3px;
  line-height: 17.5px;
`;

const MyReadValue = styled.div`
  color: #f96;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 21px;
  line-height: 28px;
`;

const ProgressSection = styled.div`
  display: flex;
  gap: 3.5px;
  flex-direction: column;
`;

const ProgressInfo = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const ProgressLabel = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 10.5px;
  line-height: 14px;
`;

const ProgressValue = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 10.5px;
  line-height: 14px;
`;

const ProgressBar = styled.div`
  overflow: hidden;

  width: 100%;
  height: 7px;
  border-radius: 50px;

  background: #e2e8f0;
`;

const ProgressFill = styled.div`
  height: 100%;
  border-radius: 50px;

  background: #f96;

  transition: width 0.3s ease;
`;
