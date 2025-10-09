import styled from '@emotion/styled';
import firecrackerIcon from '#/assets/avif/fire-cracker.avif';

function EmptyUnreadCard() {
  return (
    <Container>
      <img src={firecrackerIcon} alt="firecracker" width={96} height={96} />
      <EmptyTitle>축하합니다! 오늘 모든 뉴스레터를 읽으셨네요!</EmptyTitle>

      <DescriptionWrapper>
        <Lead>새로운 지식으로 가득한 하루를 보내셨습니다.</Lead>
        <Support>내일도 봄봄과 함께해요!</Support>
      </DescriptionWrapper>
    </Container>
  );
}

export default EmptyUnreadCard;

const Container = styled.section`
  width: 100%;
  padding: 20px;

  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const EmptyTitle = styled.h4`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;

const DescriptionWrapper = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;

const Lead = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
  text-align: center;
`;

const Support = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;
