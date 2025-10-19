import { useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { Suspense, useEffect, useState } from 'react';
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
  const [selectedDeleteIds, setSelectedDeleteIds] = useState<number[]>([]);

  const articleList = articles?.content || [];
  const haveNoContent = articleList.length === 0;

  const enableEditMode = () => {
    setEditMode(true);
  };

  const allChecked = selectedDeleteIds.length === articleList.length;

  const changeAllSelectedDeleteIds = () => {
    if (selectedDeleteIds.length === articleList.length) {
      setSelectedDeleteIds([]);
    } else {
      setSelectedDeleteIds(
        articleList.map((article) => article.articleId) || [],
      );
    }
  };

  const changeSelectedDeleteIds = (id: number) => {
    if (selectedDeleteIds.includes(id)) {
      setSelectedDeleteIds(
        selectedDeleteIds.filter((selectedId) => selectedId !== id),
      );
    } else {
      setSelectedDeleteIds([...selectedDeleteIds, id]);
    }
  };

  useEffect(() => {
    resetPage();
  }, [baseQueryParams.keyword, resetPage]);

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
        allChecked={allChecked}
        onAllSelectClick={changeAllSelectedDeleteIds}
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
            checkedIds={selectedDeleteIds}
            onCheck={changeSelectedDeleteIds}
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
