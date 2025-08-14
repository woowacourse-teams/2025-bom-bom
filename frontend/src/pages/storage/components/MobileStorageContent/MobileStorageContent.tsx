import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';
import ArticleList from '../ArticleList/ArticleList';
import SearchAndSort from '../SearchAndSort/SearchAndSort';
import { GetArticlesParams } from '@/apis/articles';
import useInfiniteArticles from '@/pages/storage/hooks/useInfiniteArticles';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

interface MobileStorageContentProps {
  baseQueryParams: GetArticlesParams;
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
}

export default function MobileStorageContent({
  baseQueryParams,
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
}: MobileStorageContentProps) {
  const loadMoreRef = useRef<HTMLDivElement>(null);

  const {
    data: infiniteArticles,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading: isInfiniteLoading,
  } = useInfiniteArticles({ baseQueryParams, isPc: false });

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

  const infiniteArticlesPages = infiniteArticles?.pages || [];
  const articleList = infiniteArticlesPages.flatMap(
    (page) => page?.content || [],
  );
  const totalElements = infiniteArticlesPages[0]?.totalElements;
  const isLoadingOrHaveContent = isInfiniteLoading || articleList.length > 0;

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
      {/* 무한 스크롤 로딩 트리거 */}
      <LoadMoreTrigger ref={loadMoreRef} />
      {isFetchingNextPage && <LoadingSpinner>로딩 중...</LoadingSpinner>}
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
