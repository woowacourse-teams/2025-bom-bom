import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { theme } from '@bombom/shared/theme';
import RequireLogin from '@/hocs/RequireLogin';
import { useDevice } from '@/hooks/useDevice';
import MobileStorageContent from '@/pages/storage/components/MobileStorageContent/MobileStorageContent';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import NewsletterFilterSkeleton from '@/pages/storage/components/NewsletterFilter/NewsletterFilterSkeleton';
import PCStorageContent from '@/pages/storage/components/PCStorageContent/PCStorageContent';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import { useStorageFilters } from '@/pages/storage/hooks/useStorageFilters';
import StorageIcon from '#/assets/svg/storage.svg';

export const Route = createFileRoute('/_bombom/storage')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 뉴스레터 보관함',
      },
    ],
  }),
  component: () => (
    <RequireLogin>
      <Storage />
    </RequireLogin>
  ),
});

function Storage() {
  const device = useDevice();
  const isPC = device === 'pc';
  const isMobile = device === 'mobile';

  const {
    selectedNewsletterId,
    sortFilter,
    searchInput,
    baseQueryParams,
    newsletterCounts,
    isLoading,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
    page,
    resetPage,
  } = useStorageFilters();

  return (
    <Container>
      {!isMobile && (
        <TitleWrapper>
          <TitleIconBox>
            <StorageIcon color={theme.colors.white} />
          </TitleIconBox>
          <Title>뉴스레터 보관함</Title>
        </TitleWrapper>
      )}

      <ContentWrapper isPC={isPC}>
        <SidebarSection isPC={isPC}>
          {isLoading ? (
            <NewsletterFilterSkeleton />
          ) : (
            <NewsLetterFilter
              newsLetterList={[
                {
                  id: 0,
                  name: '전체',
                  articleCount: newsletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newsletterCounts?.newsletters
                  .map((newsletter) => ({
                    ...newsletter,
                    articleCount: newsletter.articleCount ?? 0,
                  }))
                  .filter((newsletter) => newsletter.articleCount !== 0) ?? []),
              ]}
              selectedNewsletterId={selectedNewsletterId}
              onSelectNewsletter={handleNewsletterChange}
            />
          )}
          <QuickMenu />
        </SidebarSection>
        <MainContentSection isPC={isPC}>
          {isPC ? (
            <PCStorageContent
              baseQueryParams={baseQueryParams}
              searchInput={searchInput}
              onSearchChange={handleSearchChange}
              sortFilter={sortFilter}
              onSortChange={handleSortChange}
              onPageChange={handlePageChange}
              page={page}
              resetPage={resetPage}
            />
          ) : (
            <MobileStorageContent
              baseQueryParams={baseQueryParams}
              searchInput={searchInput}
              onSearchChange={handleSearchChange}
              sortFilter={sortFilter}
              onSortChange={handleSortChange}
              resetPage={resetPage}
            />
          )}
        </MainContentSection>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const TitleIconBox = styled.div`
  width: 28px;
  height: 28px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const ContentWrapper = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isPC }) => (isPC ? '32px' : 0)};
  flex-direction: ${({ isPC }) => (isPC ? 'row' : 'column')};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ isPC: boolean }>`
  width: ${({ isPC }) => (isPC ? '320px' : '100%')};

  display: flex;
  gap: ${({ isPC }) => (isPC ? '20px' : '8px')};
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? 2 : 0)};
`;

const MainContentSection = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: 16px;
  flex: 1;
  flex-direction: column;

  order: 1;
`;
