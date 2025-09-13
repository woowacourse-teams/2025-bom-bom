import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDeviceType } from '@/hooks/useDeviceType';
import PCMemoContent from '@/pages/memo/components/PCMemoContent/PCMemoContent';
import { useMemoFilters } from '@/pages/memo/hooks/useMemoFilters';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import { theme } from '@/styles/theme';
import MemoIcon from '#/assets/memo.svg';

export const Route = createFileRoute('/_bombom/memo')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
    ],
  }),
  component: MemoPage,
});

function MemoPage() {
  const deviceType = useDeviceType();
  const isPC = deviceType === 'pc';

  const {
    selectedNewsletterId,
    baseQueryParams,
    newletterCounts,
    handleNewsletterChange,
    handlePageChange,
    page,
    resetPage,
  } = useMemoFilters();

  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <BookmarkStorageIcon fill={theme.colors.white} />
          <Title>메모 보관함</Title>
        </TitleWrapper>

        <ContentWrapper isPC={isPC}>
          <SidebarSection isPC={isPC}>
            <NewsLetterFilter
              newsLetterList={[
                {
                  id: 0,
                  name: '전체',
                  articleCount: newletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newletterCounts?.newsletters
                  .filter((newsletter) => newsletter.highlightCount > 0)
                  .map((newsletter) => ({
                    id: newsletter.id,
                    name: newsletter.name,
                    articleCount: newsletter.highlightCount,
                    imageUrl: newsletter.imageUrl,
                  })) ?? []),
              ]}
              selectedNewsletterId={selectedNewsletterId}
              onSelectNewsletter={handleNewsletterChange}
            />
          </SidebarSection>

          <MainContentSection isPC={isPC}>
            <PCMemoContent
              baseQueryParams={baseQueryParams}
              onPageChange={handlePageChange}
              page={page}
              resetPage={resetPage}
            />
          </MainContentSection>
        </ContentWrapper>
      </MainSection>
    </Container>
  );
}

export default MemoPage;

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

const ContentWrapper = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isPC }) => (isPC ? 32 : 20)}px;
  flex-direction: ${({ isPC }) => (isPC ? 'row' : 'column')};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ isPC: boolean }>`
  width: ${({ isPC }) => (isPC ? '320px' : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? 1 : 0)};
`;

const MainContentSection = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  flex: 1;
  flex-direction: column;

  order: ${({ isPC }) => (isPC ? 2 : 1)};
`;

const BookmarkStorageIcon = styled(MemoIcon)`
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
