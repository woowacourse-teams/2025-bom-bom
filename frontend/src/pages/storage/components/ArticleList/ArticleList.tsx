import styled from '@emotion/styled';
import { Device, useDeviceType } from '@/hooks/useDeviceType';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
}

export default function ArticleList({ articles }: ArticleListProps) {
  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
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

const Container = styled.ul<{ deviceType: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'mobile' ? '0' : '16px')};
  flex-direction: column;

  ${({ deviceType, theme }) =>
    deviceType === 'mobile' &&
    `
    li {
      padding: 8px 0;
    }
    li:not(:last-child) {
      border-bottom: 2px solid ${theme.colors.dividers};
    }
  `}
`;
