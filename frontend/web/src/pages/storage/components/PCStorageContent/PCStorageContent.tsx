import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useSelectedDeleteIds } from '../../hooks/useSelectedDeleteIds';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { GetArticlesParams } from '@/apis/articles';

interface PCStorageContentProps {
  baseQueryParams: GetArticlesParams;
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  onPageChange: (page: number) => void;
  page: number;
  resetPage: () => void;
}

export default function PCStorageContent({
  baseQueryParams,
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
  onPageChange,
  page,
  resetPage,
}: PCStorageContentProps) {
  const { data: articles, isLoading } = useQuery(
    queries.articles({
      ...baseQueryParams,
      newsletterId: baseQueryParams.newsletterId || undefined,
      page: page - 1,
    }),
  );
  const [editMode, setEditMode] = useState(false);
  const articleList = articles?.content || [];
  const {
    selectedIds,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  } = useSelectedDeleteIds(articleList);

  const haveNoContent = articleList.length === 0;

  const enableEditMode = () => {
    setEditMode(true);
  };

  useEffect(() => {
    resetPage();
  }, [baseQueryParams.keyword, resetPage]);

  useEffect(() => {
    clearSelection();
  }, [searchInput, sortFilter, page, clearSelection]);

  return (
    <>
      <ArticleListControls
        searchInput={searchInput}
        onSearchChange={onSearchChange}
        sortFilter={sortFilter}
        onSortChange={onSortChange}
        editMode={editMode}
        onSelectDeleteButtonClick={enableEditMode}
        onDeleteButtonClick={() => console.log('delete')}
        allChecked={isAllSelected}
        onAllSelectClick={toggleSelectAll}
      />
      {haveNoContent && searchInput !== '' ? (
        <EmptySearchCard searchQuery={searchInput} />
      ) : haveNoContent ? (
        <EmptyLetterCard title="보관된 뉴스레터가 없어요" />
      ) : isLoading ? (
        <ArticleCardListSkeleton />
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
