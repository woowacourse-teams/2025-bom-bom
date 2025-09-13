import styled from '@emotion/styled';

interface LeaderboardItemProps {
  rank: number;
  name: string;
  readCount: number;
}

const LeaderboardItem = ({ rank, name, readCount }: LeaderboardItemProps) => (
  <Container>
    <RankIconWrapper>
      {rank === 1 ? '👑' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : `#${rank}`}
    </RankIconWrapper>

    <UserInfoBox>
      <UserName weight={rank === 1 ? 'normal' : 'medium'}>{name}</UserName>
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

const UserName = styled.div<{ weight: 'normal' | 'medium' }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ReadCount = styled.div`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;
