import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';
import { useSelectedDeleteIds } from '../../hooks/useSelectedDeleteIds';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import useInfiniteArticles from '@/pages/storage/hooks/useInfiniteArticles';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { GetArticlesParams } from '@/apis/articles';

interface MobileStorageContentProps {
  baseQueryParams: GetArticlesParams;
  editMode: boolean;
  enableEditMode: () => void;
  disableEditMode: () => void;
  deleteArticles: (articleIds: number[]) => void;
  resetPage: () => void;
}

export default function MobileStorageContent({
  baseQueryParams,
  editMode,
  enableEditMode,
  disableEditMode,
  deleteArticles,
  resetPage,
}: MobileStorageContentProps) {
  const loadMoreRef = useRef<HTMLDivElement>(null);

  const {
    data: infiniteArticles,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading: isInfiniteLoading,
  } = useInfiniteArticles({ baseQueryParams, isPc: false });

  const infiniteArticlesPages = infiniteArticles?.pages || [];
  const articleList = infiniteArticlesPages.flatMap(
    (page) => page?.content || [],
  );
  const IsContentsEmpty = !isInfiniteLoading && articleList.length === 0;

  const {
    selectedIds,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  } = useSelectedDeleteIds(articleList);

  useEffect(() => {
    if (!loadMoreRef.current) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 },
    );

    observer.observe(loadMoreRef.current);

    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  useEffect(() => {
    resetPage();
    clearSelection();
  }, [
    baseQueryParams.keyword,
    baseQueryParams.newsletterId,
    baseQueryParams.sort,
    clearSelection,
    resetPage,
  ]);

  return (
    <>
      <ArticleListControls
        editMode={editMode}
        onEnterEditMode={enableEditMode}
        onExitEditMode={disableEditMode}
        onDeleteSelected={() => deleteArticles(selectedIds)}
        checkedCount={selectedIds.length}
        isAllSelected={isAllSelected}
        onToggleSelectAll={toggleSelectAll}
      />
      {isInfiniteLoading ? (
        <ArticleCardListSkeleton />
      ) : IsContentsEmpty && baseQueryParams.keyword !== '' ? (
        <EmptySearchCard searchQuery={baseQueryParams.keyword ?? ''} />
      ) : IsContentsEmpty ? (
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
          {/* 무한 스크롤 로딩 트리거 */}
          <LoadMoreTrigger ref={loadMoreRef} />
          {isFetchingNextPage && <LoadingSpinner>로딩 중...</LoadingSpinner>}
        </>
      )}
    </>
  );
}

const LoadMoreTrigger = styled.div`
  width: 100%;
  height: 20px;
`;

const LoadingSpinner = styled.div`
  padding: 20px;

  display: flex;
  align-items: center;
  justify-content: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
