import styled from '@emotion/styled';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
}

export default function ArticleList({ articles }: ArticleListProps) {
  return (
    <Container>
      {articles.map((article) =>
        article ? (
          <li key={article.articleId}>
            <ArticleCard data={article} readVariant="badge" />
          </li>
        ) : null,
      )}
    </Container>
  );
}

const Container = styled.ul`
  width: 100%;

  display: flex;
  gap: 16px;
  flex-direction: column;
`;
