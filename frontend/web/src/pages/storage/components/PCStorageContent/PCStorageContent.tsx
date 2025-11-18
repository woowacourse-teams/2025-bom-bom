import { useEffect } from 'react';
import useArticles from '../../hooks/useArticles';
import { useSelectedDeleteIds } from '../../hooks/useSelectedDeleteIds';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import Pagination from '@/components/Pagination/Pagination';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

interface PCStorageContentProps {
  baseQueryParams: GetArticlesWithSearchParams;
  editMode: boolean;
  enableEditMode: () => void;
  disableEditMode: () => void;
  deleteArticles: (articleIds: number[]) => void;
  onPageChange: (page: number) => void;
  page: number;
  resetPage: () => void;
}

export default function PCStorageContent({
  baseQueryParams,
  editMode,
  enableEditMode,
  disableEditMode,
  deleteArticles,
  onPageChange,
  page,
  resetPage,
}: PCStorageContentProps) {
  const queryParams = {
    ...baseQueryParams,
    page: (baseQueryParams.page ?? 1) - 1,
  };

  const { data: articles, isLoading } = useArticles(queryParams);
  const articleList = articles?.content || [];

  const {
    selectedIds,
    hasBookmarkedArticles,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  } = useSelectedDeleteIds(articleList);

  const haveNoContent = articleList.length === 0;

  const handleDeleteArticles = () => {
    deleteArticles(selectedIds);
    clearSelection();
  };

  useEffect(() => {
    resetPage();
  }, [
    baseQueryParams.newsletterId,
    baseQueryParams.keyword,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(baseQueryParams.sort),
    resetPage,
  ]);

  useEffect(() => {
    clearSelection();
  }, [
    baseQueryParams.newsletterId,
    baseQueryParams.keyword,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(baseQueryParams.sort),
    baseQueryParams.page,
    clearSelection,
  ]);

  return (
    <>
      <ArticleListControls
        editMode={editMode}
        onEnterEditMode={enableEditMode}
        onExitEditMode={disableEditMode}
        onDeleteSelected={handleDeleteArticles}
        checkedCount={selectedIds.length}
        isAllSelected={isAllSelected}
        onToggleSelectAll={toggleSelectAll}
        hasBookmarkedArticles={hasBookmarkedArticles}
      />
      {isLoading ? (
        <ArticleCardListSkeleton />
      ) : haveNoContent && baseQueryParams.keyword !== '' ? (
        <EmptySearchCard searchQuery={baseQueryParams.keyword ?? ''} />
      ) : haveNoContent ? (
        <EmptyLetterCard title="보관된 뉴스레터가 없어요" />
      ) : (
        <>
          <ArticleList
            articles={articleList}
            editMode={editMode}
            checkedIds={selectedIds}
            onCheck={toggleSelect}
            onDeleteArticle={(articleIds) => deleteArticles(articleIds)}
          />
          <Pagination
            currentPage={page}
            totalPages={articles?.totalPages ?? 1}
            onPageChange={onPageChange}
          />
        </>
      )}
    </>
  );
}
