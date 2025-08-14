import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { queries } from '@/apis/queries';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';

export const Route = createFileRoute('/_bombom/bookmark')({
  component: BookmarkPage,
});

function BookmarkPage() {
  const { data: articles } = useQuery(queries.bookmarks());
  const [selectedNewsletter, setSelectedNewsletter] = useState('전체');

  const handleNewsletterChange = useCallback((value: string) => {
    setSelectedNewsletter(value);
  }, []);

  const { data: newletterCounts } = useQuery(
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

        <ContentWrapper>
          <SidebarSection>
            <NewsLetterFilter
              newsLetterList={[
                {
                  newsletter: '전체',
                  count: newletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newletterCounts?.newsletters.filter(
                  (newsletter) => newsletter.count !== 0,
                ) ?? []),
              ]}
              selectedNewsletter={selectedNewsletter}
              onSelectNewsletter={handleNewsletterChange}
            />
          </SidebarSection>

          <MainContentSection>
            {articles.content?.length && articles.content?.length > 0 ? (
              <ArticleList>
                {articles.content?.map((article) => (
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
