import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import ArrowIcon from '@/components/icons/ArrowIcon';
import AvatarIcon from '#/assets/avatar.svg';

interface LeaderboardItemProps {
  rank: number;
  name: string;
  readCount: number;
  isCrown: boolean;
  badgeText?: string;
}

const LeaderboardItem = ({
  rank,
  name,
  readCount,
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
      </StatsContainer>
    </UserInfo>

    <BookIconContainer>
      <ArrowIcon direction="upRight" />
    </BookIconContainer>
  </ItemContainer>
);

export default function ReadingKingLeaderboard() {
  const { data: monthlyReadingRank, isLoading } = useQuery(
    queries.monthlyReadingRank({ limit: 5 }),
  );
  const { data: userRank } = useQuery(queries.myMonthlyReadingRank());

  console.log(userRank);

  if (isLoading) {
    return (
      <Container>
        <Header>
          <TitleContainer>
            <HeaderIcon>
              <ArrowIcon direction="upRight" />
            </HeaderIcon>
            <Title>이달의 독서왕</Title>
          </TitleContainer>
        </Header>
        <LoadingMessage>데이터를 불러오는 중...</LoadingMessage>
      </Container>
    );
  }

  return (
    <Container>
      <Header>
        <TitleContainer>
          <HeaderIcon>
            <ArrowIcon direction="upRight" />
          </HeaderIcon>
          <Title>이달의 독서왕</Title>
        </TitleContainer>
      </Header>

      <LeaderboardList>
        {monthlyReadingRank &&
          monthlyReadingRank.length > 0 &&
          monthlyReadingRank.map((item) => (
            <LeaderboardItem
              key={item.rank}
              rank={item.rank}
              name={item.nickname}
              readCount={item.monthlyReadCount}
              isCrown={item.rank === 1}
            />
          ))}
      </LeaderboardList>

      {userRank && (
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
              {/* <ProgressBar>
                <ProgressFill
                  style={{
                    width: `${userRank.progressPercentage}%`,
                  }}
                />
              </ProgressBar> */}
            </ProgressSection>
          </MyRankContainer>
        </MyRankSection>
      )}
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 400px;
  padding: 22px;
  border: 1px solid rgb(226 232 240 / 100%);
  border-radius: 21px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  display: flex;
  gap: 21px;
  flex-direction: column;

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
  width: 28px;
  height: 28px;
  border-radius: 12.75px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};

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
  padding: 10.5px;
  border-radius: 12.75px;

  display: flex;
  align-items: center;
`;

const RankIconContainer = styled.div`
  width: 17.5px;
  height: 17.5px;
  margin-right: 10.5px;

  display: flex;
  align-items: center;
  justify-content: center;
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
  padding-bottom: 1px;

  display: flex;
  flex: 1;
  flex-direction: column;
`;

const NameContainer = styled.div`
  margin-bottom: -1px;

  display: flex;
  gap: 7px;
  align-items: center;
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
  margin-bottom: -1px;

  display: flex;
  gap: 7px;
  align-items: center;
`;

const ReadCount = styled.div`
  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 12.3px;
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
  margin-bottom: 10.5px;
  padding: 13px 14px 14px;
  border-radius: 14px;

  display: flex;
  gap: 12px;
  flex-direction: column;
  justify-content: space-between;

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

// const ProgressBar = styled.div`
//   overflow: hidden;
//   width: 100%;
//   height: 7px;
//   border-radius: 50px;

//   background: #e2e8f0;
// `;

// const ProgressFill = styled.div`
//   height: 100%;
//   border-radius: 50px;

//   background: #f96;

//   transition: width 0.3s ease;
// `;

const LoadingMessage = styled.div`
  padding: 40px 20px;

  color: #45556c;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-size: 14px;
  text-align: center;
`;
