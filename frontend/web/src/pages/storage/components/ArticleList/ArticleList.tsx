import styled from '@emotion/styled';
import Checkbox from '@/components/Checkbox/Checkbox';
import { useDevice } from '@/hooks/useDevice';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import type { Device } from '@/hooks/useDevice';
import type { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
  editMode?: boolean;
  checkedIds?: number[];
  onCheck?: (id: number) => void;
}

const ArticleList = ({
  articles,
  editMode,
  checkedIds,
  onCheck,
}: ArticleListProps) => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container device={device}>
      {articles.map((article) => (
        <ArticleItem key={article.articleId} isMobile={isMobile}>
          {editMode && checkedIds && (
            <Checkbox
              id={article.articleId}
              checked={checkedIds.includes(article.articleId)}
              onChange={() => onCheck?.(article.articleId)}
            />
          )}
          <ArticleCard data={article} readVariant="badge" />
        </ArticleItem>
      ))}
    </Container>
  );
};

export default ArticleList;

const Container = styled.ul<{ device: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ device }) => (device === 'mobile' ? '0' : '16px')};
  flex-direction: column;
`;

const ArticleItem = styled.li<{ isMobile: boolean }>`
  padding: ${({ isMobile }) => isMobile && '8px 0'};

  display: flex;
  gap: 8px;

  &:not(:last-child) {
    border-bottom: ${({ theme, isMobile }) =>
      isMobile && `2px solid ${theme.colors.dividers}`};
  }
`;
