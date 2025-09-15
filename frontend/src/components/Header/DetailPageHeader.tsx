import styled from '@emotion/styled';
import { useRouter } from '@tanstack/react-router';
import ChevronIcon from '@/components/icons/ChevronIcon';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';

interface DetailPageHeaderProps {
  bookmarked?: boolean;
  onBookmarkClick?: (bookmarked: boolean) => void;
}

const DetailPageHeader = ({
  bookmarked = false,
  onBookmarkClick,
}: DetailPageHeaderProps) => {
  const router = useRouter();

  const handleBackClick = () => {
    router.history.back();
  };

  return (
    <Container>
      <BackButton type="button" onClick={handleBackClick} aria-label="뒤로가기">
        <BackIcon direction="left" />
      </BackButton>
      <BookmarkButton
        type="button"
        onClick={() => onBookmarkClick?.(bookmarked)}
      >
        <BookmarkIcon
          as={bookmarked ? BookmarkActiveIcon : BookmarkInactiveIcon}
          width={28}
          height={28}
        />
      </BookmarkButton>
    </Container>
  );
};

export default DetailPageHeader;

const Container = styled.header`
  position: fixed;
  top: 0;
  z-index: ${({ theme }) => theme.zIndex.header};
  width: 100%;
  height: calc(
    ${({ theme }) => theme.heights.headerMobile} + env(safe-area-inset-top)
  );

  /* padding: 4px 8px; */
  padding-top: calc(4px + env(safe-area-inset-top));
  box-shadow:
    0 8px 12px -6px rgb(0 0 0 / 10%),
    0 3px 5px -4px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: space-between;

  background: ${({ theme }) => theme.colors.white};
`;

const BackButton = styled.button`
  padding: 4px;

  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.textPrimary};

  & > svg {
    transition: transform 0.2s ease;
  }

  &:hover > svg {
    transform: scale(1.1);
  }
`;

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

const BookmarkIcon = styled.svg`
  color: ${({ theme }) => theme.colors.primary};
`;

const BackIcon = styled(ChevronIcon)`
  width: 32px;
  height: 32px;
`;
