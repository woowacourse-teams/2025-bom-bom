import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { getBookmarkArticles } from '@/apis/bookmark';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';

const BookmarkPage = () => {
  const { data: articles } = useQuery({
    queryKey: ['bookmarkArticles'],
    queryFn: () => getBookmarkArticles(),
  });

  if (!articles) return null;

  return (
    <Container>
      <TitleWrapper>
        <BookmarkStorageIcon />
        <Title>북마크 보관함</Title>
      </TitleWrapper>
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
    </Container>
  );
};

export const Route = createFileRoute('/_bombom/bookmark')({
  component: BookmarkPage,
});

export default BookmarkPage;

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
`;

const ArticleList = styled.ul`
  width: 100%;

  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
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
