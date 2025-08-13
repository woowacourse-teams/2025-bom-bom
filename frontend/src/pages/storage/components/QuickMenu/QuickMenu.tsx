import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import { useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';
import MemoIcon from '#/assets/memo.svg';
import QuickMenuIcon from '#/assets/quick-menu.svg';

const QuickMenu = () => {
  const deviceType = useDeviceType();

  return (
    <Container>
      {deviceType === 'pc' && (
        <TitleWrapper>
          <QuickMenuIconWrapper>
            <StyledQuickMenuIcon />
          </QuickMenuIconWrapper>
          <Title>바로 가기</Title>
        </TitleWrapper>
      )}
      <ButtonContainer>
        <ButtonWrapper>
          <StyledBookmarkIcon />
          <LinkButton to={'/bookmark'}>북마크</LinkButton>
        </ButtonWrapper>
        <ButtonWrapper>
          <MemoIcon width={20} height={20} fill={theme.colors.primary} />
          <LinkButton to={'/memo'}>메모</LinkButton>
        </ButtonWrapper>
      </ButtonContainer>
    </Container>
  );
};

export default QuickMenu;

const Container = styled.nav`
  width: 100%;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

const QuickMenuIconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const StyledQuickMenuIcon = styled(QuickMenuIcon)`
  width: 16px;
  height: 16px;

  color: ${({ theme }) => theme.colors.white};
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

const ButtonWrapper = styled.div`
  padding: 8px;
  border-radius: 8px;

  display: flex;
  gap: 4px;
  align-items: center;
`;

const StyledBookmarkIcon = styled(BookmarkIcon)`
  width: 20px;
  height: 20px;

  color: ${({ theme }) => theme.colors.primary};
`;

const LinkButton = styled(Link)`
  font: ${({ theme }) => theme.fonts.body1};
`;

const ButtonContainer = styled.div`
  display: flex;
  gap: 16px;
  align-items: center;
`;
