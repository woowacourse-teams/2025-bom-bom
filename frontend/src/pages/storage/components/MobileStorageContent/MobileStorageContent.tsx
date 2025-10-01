import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';
import ArticleList from '../ArticleList/ArticleList';
import ArticleListControls from '../ArticleListControls/ArticleListControls';
import EmptySearchCard from '../EmptySearchCard/EmptySearchCard';
import useInfiniteArticles from '@/pages/storage/hooks/useInfiniteArticles';
import ArticleCardListSkeleton from '@/pages/today/components/ArticleCardList/ArticleCardListSkeleton';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import type { GetArticlesParams } from '@/apis/articles';

interface MobileStorageContentProps {
  baseQueryParams: GetArticlesParams;
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  resetPage: () => void;
}

export default function MobileStorageContent({
  baseQueryParams,
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
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
  }, [
    baseQueryParams.keyword,
    baseQueryParams.newsletterId,
    baseQueryParams.sort,
    resetPage,
  ]);

  const infiniteArticlesPages = infiniteArticles?.pages || [];
  const articleList = infiniteArticlesPages.flatMap(
    (page) => page?.content || [],
  );
  const totalElements = infiniteArticlesPages[0]?.totalElements;
  const IsContentsEmpty = !isInfiniteLoading && articleList.length === 0;

  if (IsContentsEmpty && searchInput !== '') {
    return <EmptySearchCard searchQuery={searchInput} />;
  }

  if (IsContentsEmpty) {
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
        isLoading={isInfiniteLoading}
      />
      {isInfiniteLoading ? (
        <ArticleCardListSkeleton />
      ) : (
        <ArticleList articles={articleList} />
      )}
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
