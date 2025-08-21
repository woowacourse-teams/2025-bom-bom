import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { queries } from '@/apis/queries';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';

export const Route = createFileRoute('/_bombom/bookmark')({
  component: BookmarkPage,
});

function BookmarkPage() {
  const [selectedNewsletterId, setSelectedNewsletterId] = useState<
    number | null
  >(null);
  const { data: articles } = useQuery(
    queries.bookmarks({
      newsletterId: selectedNewsletterId || undefined,
      size: 100, // 페이지네이션 없이 구현
    }),
  );
  const deviceType = useDeviceType();

  const totalElements = articles?.totalElements ?? 0;

  const handleNewsletterChange = useCallback((id: number | null) => {
    setSelectedNewsletterId(id);
  }, []);

  const { data: newsletterCounts } = useQuery(
    queries.bookmarksStatisticsNewsletters(),
  );

  if (!articles) return null;
  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <BookmarkStorageIcon />
          <Title>북마크 보관함</Title>
        </TitleWrapper>

        <ContentWrapper deviceType={deviceType}>
          <SidebarSection deviceType={deviceType}>
            <NewsLetterFilter
              newsLetterList={[
                {
                  id: 0,
                  name: '전체',
                  articleCount: newsletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newsletterCounts?.newsletters.map((newsletter) => ({
                  ...newsletter,
                  name: newsletter.name,
                  articleCount: newsletter.bookmarkCount ?? 0,
                })) ?? []),
              ]}
              selectedNewsletterId={selectedNewsletterId}
              onSelectNewsletter={handleNewsletterChange}
            />
          </SidebarSection>

          <MainContentSection deviceType={deviceType}>
            <SummaryBar>
              <ResultsInfo>총 {totalElements}개의 메모</ResultsInfo>
            </SummaryBar>
            {articles.content && articles.content?.length > 0 ? (
              <ArticleList>
                {articles.content.map((article) => (
                  <li key={article.articleId}>
                    <ArticleCard data={article} readVariant="badge" />
                  </li>
                ))}
              </ArticleList>
            ) : (
              <EmptyLetterCard title="북마크한 뉴스레터가 없어요" />
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

const ContentWrapper = styled.div<{ deviceType: DeviceType }>`
  width: 100%;

  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'pc' ? '32px' : '20px')};
  flex-direction: ${({ deviceType }) =>
    deviceType === 'pc' ? 'row' : 'column'};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ deviceType: DeviceType }>`
  width: ${({ deviceType }) => (deviceType === 'pc' ? '320px' : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;

  order: ${({ deviceType }) => (deviceType === 'pc' ? 1 : 0)};
`;

const MainContentSection = styled.div<{ deviceType: DeviceType }>`
  display: flex;
  gap: 20px;
  flex: 1;
  flex-direction: column;

  order: ${({ deviceType }) => (deviceType === 'pc' ? 2 : 1)};
`;

const ArticleList = styled.ul`
  width: 100%;

  display: flex;
  gap: 16px;
  flex-direction: column;
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
