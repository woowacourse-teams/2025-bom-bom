import styled from '@emotion/styled';
import { theme } from '@/styles/theme';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';
import ArrowIcon from '#/assets/chevron-up.svg';

interface FloatingActionButtonsProps {
  bookmarked: boolean;
  onToggleBookmarkClick: (bookmarked: boolean) => void;
}

const FloatingActionButtons = ({
  bookmarked,
  onToggleBookmarkClick,
}: FloatingActionButtonsProps) => {
  return (
    <Container>
      <ActionButton
        type="button"
        onClick={() => onToggleBookmarkClick(bookmarked)}
      >
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

      <ActionButton type="button">
        <ScrollUpIcon />
      </ActionButton>
    </Container>
  );
};

export default FloatingActionButtons;

const Container = styled.div`
  position: fixed;
  top: 60%;
  left: 20%;
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

const ScrollUpIcon = styled(ArrowIcon)`
  width: 28px;
  height: 28px;

  color: ${({ theme }) => theme.colors.icons};
`;
