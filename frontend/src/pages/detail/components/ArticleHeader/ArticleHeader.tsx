import styled from '@emotion/styled';
import Chip from '@/components/Chip/Chip';
import useMediaQuery from '@/hooks/useMediaQuery';
import { theme } from '@/styles/theme';
import { formatDate } from '@/utils/date';
import BookmarkActiveIcon from '#/assets/bookmark-active.svg';
import BookmarkInactiveIcon from '#/assets/bookmark-inactive.svg';
import ClockIcon from '#/assets/clock.svg';

interface ArticleHeaderProps {
  title: string;
  newsletterCategory: string;
  newsletterName: string;
  arrivedDateTime: Date;
  expectedReadTime: number;
  bookmarked?: boolean;
  onBookmarkClick?: (bookmarked: boolean) => void;
}

const ArticleHeader = ({
  title,
  newsletterCategory,
  newsletterName,
  arrivedDateTime,
  expectedReadTime,
  bookmarked = false,
  onBookmarkClick,
}: ArticleHeaderProps) => {
  const isBookmarkButtonVisible = useMediaQuery({
    key: 'max-width',
    value: 1350,
  });

  return (
    <Container>
      <TitleRow>
        <Title>{title}</Title>
      </TitleRow>
      <MetaInfoRow>
        <Chip text={newsletterCategory} />
        <MetaInfoText>from {newsletterName}</MetaInfoText>
        <MetaInfoText>{formatDate(arrivedDateTime)}</MetaInfoText>
        <ReadTimeBox>
          <ClockIcon width={16} height={16} />
          <MetaInfoText>{expectedReadTime}분</MetaInfoText>
        </ReadTimeBox>
        {isBookmarkButtonVisible && (
          <BookmarkButton
            type="button"
            onClick={() => onBookmarkClick?.(bookmarked)}
          >
            {bookmarked ? (
              <BookmarkActiveIcon width={24} height={24} />
            ) : (
              <BookmarkInactiveIcon
                width={24}
                height={24}
                color={theme.colors.primary}
              />
            )}
          </BookmarkButton>
        )}
      </MetaInfoRow>
    </Container>
  );
};

export default ArticleHeader;

const Container = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
`;

const TitleRow = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
`;

const Title = styled.h2`
  flex: 1;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const BookmarkButton = styled.button`
  margin-left: auto;
  padding: 8px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 50%;
  box-shadow: 0 2px 8px rgb(0 0 0 / 5%);

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

const MetaInfoRow = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  align-items: center;
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;
