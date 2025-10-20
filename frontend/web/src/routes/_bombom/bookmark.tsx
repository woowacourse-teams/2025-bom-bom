import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute, useSearch } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import Skeleton from '@/components/Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';
import ArticleList from '@/pages/storage/components/ArticleList/ArticleList';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import NewsletterFilterSkeleton from '@/pages/storage/components/NewsletterFilter/NewsletterFilterSkeleton';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { Device } from '@/hooks/useDevice';
import BookmarkIcon from '#/assets/svg/bookmark-inactive.svg';

export const Route = createFileRoute('/_bombom/bookmark')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 북마크 보관함',
      },
    ],
  }),
  component: BookmarkPage,
  validateSearch: (search: { newsletterId?: number }) => {
    return {
      newsletterId: search.newsletterId,
    };
  },
});

function BookmarkPage() {
  const newsletterId = useSearch({
    from: '/_bombom/bookmark',
    select: (state) => state.newsletterId,
  });
  const { data: articles, isLoading } = useQuery(
    queries.bookmarks({
      newsletterId: newsletterId,
      size: 100, // 페이지네이션 없이 구현
    }),
  );
  const device = useDevice();

  const totalElements = articles?.totalElements ?? 0;

  const { data: newsletterCounts } = useQuery(
    queries.bookmarksStatisticsNewsletters(),
  );

  const bookmarkContent = articles?.content ?? [];
  const haveNoContent = !isLoading && bookmarkContent.length === 0;

  if (haveNoContent)
    return <EmptyLetterCard title="북마크한 뉴스레터가 없어요" />;

  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <BookmarkStorageIcon />
          <Title>북마크 보관함</Title>
        </TitleWrapper>

        <ContentWrapper device={device}>
          <SidebarSection device={device}>
            {!newsletterCounts ? (
              <NewsletterFilterSkeleton />
            ) : (
              <NewsLetterFilter filters={newsletterCounts} />
            )}

            <QuickMenu />
          </SidebarSection>

          <MainContentSection device={device}>
            <SummaryBar>
              {isLoading ? (
                <Skeleton width="80px" height="20px" />
              ) : (
                <ResultsInfo>총 {totalElements}개의 북마크</ResultsInfo>
              )}
            </SummaryBar>
            {isLoading ? (
              <ArticleCardListSkeleton />
            ) : (
              <ArticleList articles={bookmarkContent} />
            )}
          </MainContentSection>
        </ContentWrapper>
      </MainSection>
    </Container>
  );
}

export default BookmarkPage;

const Container = styled.div`
  width: 100%;
  max-width: 1280px;

  display: flex;
  align-items: flex-start;
  justify-content: center;
`;

const MainSection = styled.div`
  width: 100%;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: flex-start;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const ContentWrapper = styled.div<{ device: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ device }) => (device === 'pc' ? '32px' : '20px')};
  flex-direction: ${({ device }) => (device === 'pc' ? 'row' : 'column')};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ device: Device }>`
  width: ${({ device }) => (device === 'pc' ? '320px' : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const MainContentSection = styled.div<{ device: Device }>`
  display: flex;
  gap: 20px;
  flex: 1;
  flex-direction: column;

  order: ${({ device }) => (device === 'pc' ? 2 : 1)};
`;

const BookmarkStorageIcon = styled(BookmarkIcon)`
  width: 36px;
  height: 36px;
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  text-align: center;
`;

const SummaryBar = styled.div`
  width: 100%;
  margin-bottom: 24px;

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const ResultsInfo = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
