import styled from '@emotion/styled';

const RANK_ICON_MAP: Record<number, string> = {
  1: '👑',
  2: '🥈',
  3: '🥉',
};

interface LeaderboardItemProps {
  rank: number;
  name: string;
  readCount: number;
}

const LeaderboardItem = ({ rank, name, readCount }: LeaderboardItemProps) => (
  <Container>
    <RankIconWrapper>{RANK_ICON_MAP[rank] ?? `#${rank}`}</RankIconWrapper>

    <UserInfoBox>
      <UserName>{name}</UserName>
      <ReadCount>{readCount}개 읽음</ReadCount>
    </UserInfoBox>
  </Container>
);

export default LeaderboardItem;

const Container = styled.div`
  padding: 10px;
  border-radius: 12px;

  display: flex;
  gap: 10px;
  align-items: center;
`;

const RankIconWrapper = styled.div`
  width: 24px;
  height: 24px;

  display: flex;
  align-items: center;
  justify-content: center;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;

const UserInfoBox = styled.div`
  display: flex;
  gap: 2px;
  flex-direction: column;
`;

const UserName = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ReadCount = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;
