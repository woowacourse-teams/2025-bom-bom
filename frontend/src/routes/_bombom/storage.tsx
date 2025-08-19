import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDeviceType } from '@/hooks/useDeviceType';
import MobileStorageContent from '@/pages/storage/components/MobileStorageContent/MobileStorageContent';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import PCStorageContent from '@/pages/storage/components/PCStorageContent/PCStorageContent';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import { useStorageFilters } from '@/pages/storage/hooks/useStorageFilters';
import { theme } from '@/styles/theme';
import StorageIcon from '#/assets/storage.svg';

export const Route = createFileRoute('/_bombom/storage')({
  component: Storage,
});

function Storage() {
  const deviceType = useDeviceType();
  const isPC = deviceType === 'pc';

  const {
    selectedNewsletter,
    sortFilter,
    searchInput,
    baseQueryParams,
    newletterCounts,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
    page,
    resetPage,
  } = useStorageFilters();

  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <TitleIconBox>
            <StorageIcon color={theme.colors.white} />
          </TitleIconBox>
          <Title>뉴스레터 보관함</Title>
        </TitleWrapper>

        <ContentWrapper isPC={isPC}>
          <SidebarSection isPC={isPC}>
            <NewsLetterFilter
              newsLetterList={[
                {
                  newsletter: '전체',
                  name: '전체',
                  articleCount: newletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newletterCounts?.newsletters
                  .map((newsletter) => ({
                    ...newsletter,
                    count: newsletter.articleCount ?? 0,
                  }))
                  .filter((newsletter) => newsletter.count !== 0) ?? []),
              ]}
              selectedNewsletter={selectedNewsletter}
              onSelectNewsletter={handleNewsletterChange}
            />
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
      </MainSection>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

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
  font: ${({ theme }) => theme.fonts.heading2};
`;

const ContentWrapper = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isPC }) => (isPC ? 32 : 20)}px;
  flex-direction: ${({ isPC }) => (isPC ? 'row' : 'column')};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ isPC: boolean }>`
  width: 320px;
  min-width: ${({ isPC }) => (isPC ? 320 : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? 1 : 0)};
`;

const MainContentSection = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: 40px;
  flex: 1;
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? 2 : 1)};
`;
