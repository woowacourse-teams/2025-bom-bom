import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
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

  useEffect(() => {
    resetPage();
  }, [baseQueryParams.keyword, resetPage]);

  const totalElements = articles?.totalElements;
  const articleList = articles?.content || [];
  const haveNoContent = !isLoading && articleList.length === 0;

  if (haveNoContent && searchInput !== '') {
    return <EmptySearchCard searchQuery={searchInput} />;
  }

  if (haveNoContent) {
    return <EmptyLetterCard title="보관된 뉴스레터가 없어요" />;
  }

  return (
    <>
      <ArticleListControls
        searchInput={searchInput}
        onSearchChange={onSearchChange}
        sortFilter={sortFilter}
        onSortChange={onSortChange}
        totalElements={totalElements}
        isLoading={isLoading}
      />
      <ArticleList articles={articleList} isLoading={isLoading} />
      <Pagination
        currentPage={page}
        totalPages={articles?.totalPages ?? 1}
        onPageChange={onPageChange}
      />
    </>
  );
}
