import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import { theme } from '@/styles/theme';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';
import MemoIcon from '#/assets/memo.svg';
import QuickMenuIcon from '#/assets/quick-menu.svg';

const QuickMenu = () => {
  return (
    <Container>
      <TitleWrapper>
        <QuickMenuIconWrapper>
          <StyledQuickMenuIcon />
        </QuickMenuIconWrapper>
        <Title>바로 가기</Title>
      </TitleWrapper>
      <BookmarkWrapper>
        <StyledBookmarkIcon />
        <BookmarkLink to={'/bookmark'}>북마크</BookmarkLink>
      </BookmarkWrapper>
      <BookmarkWrapper>
        <MemoIcon width={20} height={20} fill={theme.colors.primary} />
        <BookmarkLink to={'/memo'}>메모</BookmarkLink>
      </BookmarkWrapper>
    </Container>
  );
};

export default QuickMenu;

const Container = styled.nav`
  width: 310px;
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

const BookmarkWrapper = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;

const StyledBookmarkIcon = styled(BookmarkIcon)`
  width: 20px;
  height: 20px;

  color: ${({ theme }) => theme.colors.primary};
`;

const BookmarkLink = styled(Link)`
  font: ${({ theme }) => theme.fonts.body1};
`;
