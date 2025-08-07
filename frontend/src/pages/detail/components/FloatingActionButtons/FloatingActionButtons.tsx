import styled from '@emotion/styled';
import { useState } from 'react';
import Toast from '@/components/Toast/Toast';
import useToast from '@/components/Toast/useToast';
import { theme } from '@/styles/theme';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';
import ArrowIcon from '#/assets/chevron-up.svg';

interface FloatingActionButtonsProps {
  bookmarked: boolean | null;
  onToggleBookmark: (bookmarked: boolean) => void;
}

const ADD_BOOKMARK_MESSAGE = '북마크가 추가되었습니다.';
const DELETE_BOOKMARK_MESSAGE = '북마크가 해제되었습니다.';
const TOAST_DURATION = 3000;

const FloatingActionButtons = ({
  bookmarked,
  onToggleBookmark,
}: FloatingActionButtonsProps) => {
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const { isVisible, showToast } = useToast({
    duration: TOAST_DURATION,
  });

  if (bookmarked === null) return;

  const handleToggleBookmark = () => {
    onToggleBookmark(bookmarked);

    const message = bookmarked ? DELETE_BOOKMARK_MESSAGE : ADD_BOOKMARK_MESSAGE;
    setToastMessage(message);

    showToast(message);
  };

  return (
    <>
      <Toast
        isVisible={isVisible}
        message={toastMessage ?? ''}
        duration={TOAST_DURATION}
      />

      <Container>
        <ActionButton type="button" onClick={handleToggleBookmark}>
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
    </>
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
