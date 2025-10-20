import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useSelectedDeleteIds } from '../../hooks/useSelectedDeleteIds';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { GetArticlesParams } from '@/apis/articles';
import type { UseMutateFunction } from '@tanstack/react-query';

interface PCStorageContentProps {
  baseQueryParams: GetArticlesParams;
  editMode: boolean;
  enableEditMode: () => void;
  disableEditMode: () => void;
  deleteArticles: UseMutateFunction<
    unknown,
    Error,
    { articleIds: number[] },
    unknown
  >;
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
  const queryClient = useQueryClient();
  const { data: articles, isLoading } = useQuery(queries.articles(queryParams));
  const articleList = articles?.content || [];
  const {
    selectedIds,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  } = useSelectedDeleteIds(articleList);

  const haveNoContent = articleList.length === 0;

  useEffect(() => {
    resetPage();
  }, [baseQueryParams.keyword, resetPage]);

  useEffect(() => {
    clearSelection();
  }, [
    baseQueryParams.newsletterId,
    baseQueryParams.keyword,
    baseQueryParams.sort,
    baseQueryParams.page,
    clearSelection,
  ]);

  return (
    <>
      <ArticleListControls
        editMode={editMode}
        onEnterEditMode={enableEditMode}
        onExitEditMode={disableEditMode}
        onDeleteSelected={() =>
          deleteArticles(
            { articleIds: selectedIds },
            {
              onSuccess: () => {
                queryClient.invalidateQueries({
                  queryKey: queries.articles(queryParams).queryKey,
                });
              },
            },
          )
        }
        checkedCount={selectedIds.length}
        isAllSelected={isAllSelected}
        onToggleSelectAll={toggleSelectAll}
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
