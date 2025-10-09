import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import ChevronIcon from '@/components/icons/ChevronIcon';
import BookmarkActiveIcon from '#/assets/svg/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/svg/bookmark-inactive.svg';

interface FloatingActionButtonsProps {
  bookmarked: boolean | null;
  onBookmarkClick: (bookmarked: boolean) => void;
}

const FloatingActionButtons = ({
  bookmarked,
  onBookmarkClick,
  ...props
}: FloatingActionButtonsProps) => {
  if (bookmarked === null) return null;

  const handleBookmarkClick = () => {
    onBookmarkClick(bookmarked);
  };

  const handleScrollUp = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <Container {...props}>
      <ActionButton type="button" onClick={handleBookmarkClick}>
        {bookmarked ? (
          <BookmarkActiveIcon width={28} height={28} />
        ) : (
          <BookmarkInactiveIcon
            width={28}
            height={28}
            color={theme.colors.primary}
          />
        )}
      </ActionButton>

      <ActionButton type="button" onClick={handleScrollUp}>
        <ChevronIcon
          direction="up"
          width={28}
          height={28}
          color={theme.colors.icons}
        />
      </ActionButton>
    </Container>
  );
};

export default FloatingActionButtons;

const Container = styled.div`
  z-index: ${({ theme }) => theme.zIndex.floating};
  width: 56px;
  padding: 4px 0;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 5%);

  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.dividers};

  transform: translateY(-50%);
`;

const ActionButton = styled.button`
  padding: 8px;
  border-radius: 50%;

  display: flex;
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
