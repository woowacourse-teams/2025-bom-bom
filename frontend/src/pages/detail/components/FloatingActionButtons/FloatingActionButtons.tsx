import styled from '@emotion/styled';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';
import ArrowIcon from '#/assets/chevron-up.svg';

interface FloatingActionButtonsProps {
  bookmarked: boolean;
  handleBookmarkClick: () => void;
}

const FloatingActionButtons = ({
  bookmarked,
  handleBookmarkClick,
}: FloatingActionButtonsProps) => {
  return (
    <Container>
      <ActionButton type="button" onClick={handleBookmarkClick}>
        {bookmarked ? (
          <BookmarkActiveIcon width={44} height={44} />
        ) : (
          <BookmarkInactiveIcon width={44} height={44} />
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
  width: 72px;
  padding: 8px 0;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 5%);

  display: flex;
  gap: 12px;
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
    transform: scale(1.2);
  }
`;

const ScrollUpIcon = styled(ArrowIcon)`
  width: 44px;
  height: 44px;

  color: ${({ theme }) => theme.colors.icons};
`;
