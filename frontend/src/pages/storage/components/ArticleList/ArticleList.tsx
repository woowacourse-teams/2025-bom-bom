import styled from '@emotion/styled';
import { ARTICLE_SIZE } from '../../constants/article';
import { Device, useDevice } from '@/hooks/useDevice';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import ArticleCardSkeleton from '@/pages/today/components/ArticleCard/ArticleCardSkeleton';
import { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
  isLoading: boolean;
}

const ArticleList = ({ articles, isLoading }: ArticleListProps) => {
  const device = useDevice();

  return (
    <Container device={device}>
      {isLoading
        ? Array.from({ length: ARTICLE_SIZE }).map((_, index) => (
            <li key={`skeleton-${index}`}>
              <ArticleCardSkeleton />
            </li>
          ))
        : articles.map((article) =>
            article ? (
              <li key={article.articleId}>
                <ArticleCard data={article} readVariant="badge" />
              </li>
            ) : null,
          )}
    </Container>
  );
};

export default ArticleList;

const Container = styled.ul<{ device: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ device }) => (device === 'mobile' ? '0' : '16px')};
  flex-direction: column;

  ${({ device, theme }) =>
    device === 'mobile' &&
    `
    li {
      padding: 8px 0;
    }
    li:not(:last-child) {
      border-bottom: 2px solid ${theme.colors.dividers};
    }
  `}
`;
