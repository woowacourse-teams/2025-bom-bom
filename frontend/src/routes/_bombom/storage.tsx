import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDeviceType } from '@/hooks/useDeviceType';
import CategoryFilterWithCount from '@/pages/storage/components/CategoryFilterWithCount/CategoryFilterWithCount';
import MobileStorageContent from '@/pages/storage/components/MobileStorageContent/MobileStorageContent';
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
    selectedCategory,
    sortFilter,
    searchInput,
    baseQueryParams,
    categoryCounts,
    handleCategoryChange,
    handleSortChange,
    handleSearchChange,
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
            <CategoryFilterWithCount
              selectedCategory={selectedCategory}
              onCategoryChange={handleCategoryChange}
              totalCount={categoryCounts?.totalCount ?? 0}
              existCategories={categoryCounts?.categories?.filter(
                (category) => category.count !== 0,
              )}
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
              />
            ) : (
              <MobileStorageContent
                baseQueryParams={baseQueryParams}
                searchInput={searchInput}
                onSearchChange={handleSearchChange}
                sortFilter={sortFilter}
                onSortChange={handleSortChange}
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

  order: ${({ isPC }) => (isPC ? '1' : '2')};
`;

const MainContentSection = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: 40px;
  flex: 1;
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? '2' : '1')};
`;
