import styled from '@emotion/styled';
import { useState } from 'react';
import ArticleDeleteModal from '../ArticleDeleteModal/ArticleDeleteModal';
import Checkbox from '@/components/Checkbox/Checkbox';
import useModal from '@/components/Modal/useModal';
import { useDevice } from '@/hooks/useDevice';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import type { Device } from '@/hooks/useDevice';
import type { Article } from '@/types/articles';

interface ArticleListProps {
  articles: Article[];
  editMode?: boolean;
  checkedIds?: number[];
  onCheck?: (id: number) => void;
  onDeleteArticle?: (articleIds: number[]) => void;
}

const ArticleList = ({
  articles,
  editMode,
  checkedIds,
  onCheck,
  onDeleteArticle,
}: ArticleListProps) => {
  const device = useDevice();
  const isMobile = device === 'mobile';
  const [pendingDeleteIds, setPendingDeleteIds] = useState<number[] | null>(
    null,
  );
  const { modalRef, isOpen, openModal, closeModal } = useModal();

  const handleDeleteClick = (articleIds: number[]) => {
    setPendingDeleteIds(articleIds);
    openModal();
  };

  const handleConfirmDelete = () => {
    if (pendingDeleteIds) {
      onDeleteArticle?.(pendingDeleteIds);
      setPendingDeleteIds(null);
    }
  };

  return (
    <>
      <Container device={device}>
        {articles.map((article) => (
          <ArticleItem key={article.articleId} isMobile={isMobile}>
            {editMode && checkedIds && (
              <Checkbox
                id={String(article.articleId)}
                checked={checkedIds.includes(article.articleId)}
                onChange={() => onCheck?.(article.articleId)}
              />
            )}
            <ArticleCard
              data={article}
              readVariant="badge"
              onDelete={(articleId) => handleDeleteClick([articleId])}
            />
          </ArticleItem>
        ))}
      </Container>
      <ArticleDeleteModal
        modalRef={modalRef}
        isOpen={isOpen}
        closeModal={closeModal}
        onDelete={handleConfirmDelete}
      />
    </>
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
