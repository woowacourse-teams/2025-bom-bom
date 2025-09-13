import styled from '@emotion/styled';
import ArrowIcon from '@/components/icons/ArrowIcon';
import AvatarIcon from '#/assets/avatar.svg';

interface LeaderboardItemProps {
  rank: number;
  name: string;
  readCount: number;
  badgeText?: string;
}

const LeaderboardItem = ({
  rank,
  name,
  readCount,
  badgeText,
}: LeaderboardItemProps) => (
  <Container>
    <RankIconWrapper>
      {rank === 1 ? (
        <CrownIconWrapper>👑</CrownIconWrapper>
      ) : rank === 2 ? (
        <SecondPlaceIcon>🥈</SecondPlaceIcon>
      ) : rank === 3 ? (
        <ThirdPlaceIcon>🥉</ThirdPlaceIcon>
      ) : (
        <RankNumber>#{rank}</RankNumber>
      )}
    </RankIconWrapper>

    <AvatarContainer>
      <AvatarIcon />
    </AvatarContainer>

    <UserInfo>
      <NameContainer>
        <UserName weight={rank === 1 ? 'normal' : 'medium'}>{name}</UserName>
        {badgeText && <Badge>{badgeText}</Badge>}
      </NameContainer>
      <StatsContainer>
        <ReadCount>{readCount}개 읽음</ReadCount>
      </StatsContainer>
    </UserInfo>

    <BookIconContainer>
      <ArrowIcon direction="upRight" />
    </BookIconContainer>
  </Container>
);

export default LeaderboardItem;

const Container = styled.div`
  padding: 10px;
  border-radius: 12px;

  display: flex;
  align-items: center;
`;

const RankIconWrapper = styled.div`
  width: 16px;
  height: 16px;
  margin-right: 10.5px;

  display: flex;
  align-items: center;
  justify-content: center;

  font: ${({ theme }) => theme.fonts.body1};
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
