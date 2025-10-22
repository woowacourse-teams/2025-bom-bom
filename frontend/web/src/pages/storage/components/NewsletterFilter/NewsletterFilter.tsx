import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import { parseAsInteger, useQueryState } from 'nuqs';
import Badge from '@/components/Badge/Badge';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { useDevice } from '@/hooks/useDevice';
import type {
  BookmarkFilters,
  HighlightFilters,
  NewsletterFilters,
} from '@/types/articles';
import NewsIcon from '#/assets/svg/news.svg';

type Newsletter =
  | NewsletterFilters['newsletters'][0]
  | BookmarkFilters['newsletters'][0]
  | HighlightFilters['newsletters'][0];

interface NewsLetterFilterProps {
  filters: NewsletterFilters | BookmarkFilters | HighlightFilters;
}

function NewsLetterFilter({ filters }: NewsLetterFilterProps) {
  const device = useDevice();
  const [selectedNewsletterId, setSelectedNewsletterId] = useQueryState(
    'newsletterId',
    parseAsInteger.withDefault(0),
  );

  const isNewsletterFilter = (
    newsletter: Newsletter,
  ): newsletter is NewsletterFilters['newsletters'][0] =>
    'articleCount' in newsletter;

  const isBookmarkFilter = (
    newsletter: Newsletter,
  ): newsletter is BookmarkFilters['newsletters'][0] =>
    'bookmarkCount' in newsletter;

  const newsletterFiltersWithAll = [
    {
      id: 0,
      name: '전체',
      articleCount: filters?.totalCount ?? 0,
      imageUrl: '',
    },
    ...(filters?.newsletters
      .map((newsletter) => ({
        ...newsletter,
        articleCount: isNewsletterFilter(newsletter)
          ? newsletter.articleCount
          : isBookmarkFilter(newsletter)
            ? newsletter.bookmarkCount
            : newsletter.highlightCount,
      }))
      .filter((newsletter) => newsletter.articleCount !== 0) ?? []),
  ];

  const handleSelectNewsletterId = (id: number | null) => {
    setSelectedNewsletterId(id);
  };

  return (
    <Container aria-label="뉴스레터" isPc={device === 'pc'}>
      {device === 'pc' && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={device === 'pc' ? 'vertical' : 'horizontal'}>
        {newsletterFiltersWithAll.map(
          ({ name, articleCount, imageUrl, id }) => (
            <Tab
              key={name}
              value={id ?? null}
              label={name}
              selected={
                selectedNewsletterId === null
                  ? name === '전체'
                  : id === selectedNewsletterId
              }
              onTabSelect={handleSelectNewsletterId}
              StartComponent={
                imageUrl ? <NewsLetterImage src={imageUrl} /> : null
              }
              EndComponent={<Badge text={String(articleCount)} />}
              textAlign={device === 'pc' ? 'start' : 'center'}
            />
          ),
        )}
      </StyledTabs>
    </Container>
  );
}

export default NewsLetterFilter;

export const Container = styled.nav<{ isPc: boolean }>`
  width: 100%;
  padding: ${({ isPc }) => (isPc ? '16px' : '0')};
  border: ${({ isPc, theme }) =>
    isPc ? `1px solid ${theme.colors.stroke}` : 'none'};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

export const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

export const IconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

export const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

export const NewsLetterImage = styled.img`
  width: 24px;
  height: 24px;
  border-radius: 50%;

  flex-shrink: 0;

  object-fit: cover;
`;

export const StyledTabs = styled(Tabs)`
  padding-bottom: 8px;
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 2px;
    background: ${({ theme }) => theme.colors.stroke};
  }
`;
