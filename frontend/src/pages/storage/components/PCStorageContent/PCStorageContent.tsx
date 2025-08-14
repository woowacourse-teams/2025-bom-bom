import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import ArticleList from '../ArticleList/ArticleList';
import SearchAndSort from '../SearchAndSort/SearchAndSort';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

interface PCStorageContentProps {
  baseQueryParams: GetArticlesParams;
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
}

export default function PCStorageContent({
  baseQueryParams,
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
}: PCStorageContentProps) {
  const [currentPage, setCurrentPage] = useState(1);

  const { data: articles, isLoading } = useQuery({
    ...queries.articles({
      ...baseQueryParams,
      page: currentPage - 1,
    }),
  });

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  useEffect(() => {
    setCurrentPage(1);
  }, [
    baseQueryParams.keyword,
    baseQueryParams.newsletter,
    baseQueryParams.sort,
  ]);

  const totalElements = articles?.totalElements;
  const articleList = articles?.content || [];
  const isLoadingOrHaveContent = isLoading || articleList.length > 0;

  if (!isLoadingOrHaveContent && searchInput === '')
    return <EmptyLetterCard title="보관된 뉴스레터가 없어요" />;
  return (
    <>
      <SearchAndSort
        searchInput={searchInput}
        onSearchChange={onSearchChange}
        sortFilter={sortFilter}
        onSortChange={onSortChange}
        totalElements={totalElements}
      />
      <ArticleList articles={articleList} />
      <Pagination
        currentPage={currentPage}
        totalPages={articles?.totalPages ?? 1}
        onPageChange={handlePageChange}
      />
    </>
  );
}
