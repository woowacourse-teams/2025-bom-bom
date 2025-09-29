import styled from '@emotion/styled';
import { useDevice } from '@/hooks/useDevice';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import type { Device } from '@/hooks/useDevice';
import type { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
  isLoading: boolean;
}

const ArticleList = ({ articles }: ArticleListProps) => {
  const device = useDevice();

  return (
    <Container device={device}>
      {articles.map((article) =>
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
