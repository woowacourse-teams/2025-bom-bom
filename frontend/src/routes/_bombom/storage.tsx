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
  const isPc = deviceType === 'pc';

  const {
    selectedNewsletter,
    sortFilter,
    searchInput,
    baseQueryParams,
    categoryCounts,
    existNewsletters,
    handleNewsletterChange,
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

        <ContentWrapper>
          <SidebarSection>
            <NewsLetterFilter
              newsLetterList={[
                {
                  newsletter: '전체',
                  count: categoryCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(existNewsletters ?? []),
              ]}
              selectedValue={selectedNewsletter}
              onSelectNewsletter={handleNewsletterChange}
            />
            <QuickMenu />
          </SidebarSection>
          <MainContentSection>
            {isPc ? (
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

const ContentWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 32px;
  align-items: flex-start;

  @media (width <= 1024px) {
    gap: 20px;
    flex-direction: column;
  }
`;

const SidebarSection = styled.div`
  width: 320px;

  display: flex;
  gap: 20px;
  flex-direction: column;

  @media (width <= 1024px) {
    min-width: 100%;
    order: 1;
  }
`;

const MainContentSection = styled.div`
  display: flex;
  gap: 20px;
  flex: 1;
  flex-direction: column;

  @media (width <= 1024px) {
    order: 2;
  }
`;
