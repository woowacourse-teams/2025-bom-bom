import styled from '@emotion/styled';
import { useRouter } from '@tanstack/react-router';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';
import ChevronLeftIcon from '#/assets/chevron-left.svg';

interface PageHeaderProps {
  bookmarked?: boolean;
  onBookmarkClick?: (bookmarked: boolean) => void;
}

const PageHeader = ({
  bookmarked = false,
  onBookmarkClick,
}: PageHeaderProps) => {
  const router = useRouter();

  const handleBackClick = () => {
    router.history.back();
  };

  return (
    <Container>
      <BackButton type="button" onClick={handleBackClick}>
        <ChevronLeftIcon width={28} height={28} />
      </BackButton>
      <BookmarkButton
        type="button"
        onClick={() => onBookmarkClick?.(bookmarked)}
      >
        {bookmarked ? (
          <BookmarkActiveIcon width={32} height={32} />
        ) : (
          <StyledBookmarkInactiveIcon width={32} height={32} />
        )}
      </BookmarkButton>
    </Container>
  );
};

export default PageHeader;

const Container = styled.header`
  position: fixed;
  top: 0;
  z-index: ${({ theme }) => theme.zIndex.header};
  width: 100%;
  height: calc(
    ${({ theme }) => theme.heights.headerMobile} + env(safe-area-inset-top)
  );
  padding: 4px 12px;
  padding-top: calc(4px + env(safe-area-inset-top));
  box-shadow:
    0 8px 12px -6px rgb(0 0 0 / 10%),
    0 3px 5px -4px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: space-between;

  background: ${({ theme }) => theme.colors.white};
`;

const BackButton = styled.button``;

const BookmarkButton = styled.button`
  margin-left: auto;
  padding: 8px;

  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};

  & > svg {
    transition: transform 0.2s ease;
  }

  &:hover > svg {
    transform: scale(1.1);
  }
`;

const StyledBookmarkInactiveIcon = styled(BookmarkInactiveIcon)`
  color: ${({ theme }) => theme.colors.primary};
`;
