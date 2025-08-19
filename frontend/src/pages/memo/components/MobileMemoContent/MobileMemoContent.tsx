import styled from '@emotion/styled';
import { useNavigate } from '@tanstack/react-router';
import { useEffect, useRef, useState } from 'react';
import useInfiniteMemos from '../../hooks/useInfiniteMemos';
import Select from '@/components/Select/Select';
import ReadOnlyMemoCard from '@/pages/detail/components/MemoCard/ReadOnlyMemoCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

interface MobileMemoContentProps {
  baseQueryParams: {
    sort: [string, 'DESC' | 'ASC'];
    keyword: string;
    size: number;
    newsletterId?: number;
    page: number;
  };
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  resetPage: () => void;
}

export default function MobileMemoContent({
  baseQueryParams,
  sortFilter,
  onSortChange,
  resetPage,
}: MobileMemoContentProps) {
  const navigate = useNavigate();
  const loadMoreRef = useRef<HTMLDivElement>(null);
  const [displayedItems, setDisplayedItems] = useState(baseQueryParams.size);

  const { data: allMemos, isLoading } = useInfiniteMemos({
    baseQueryParams,
    isPc: false,
  });

  const visibleMemos = allMemos?.slice(0, displayedItems) || [];
  const hasNextPage = allMemos && displayedItems < allMemos.length;

  useEffect(() => {
    if (!loadMoreRef.current) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && hasNextPage) {
          setDisplayedItems((prev) => prev + baseQueryParams.size);
        }
      },
      { threshold: 0.1 },
    );

    observer.observe(loadMoreRef.current);

    return () => observer.disconnect();
  }, [hasNextPage, baseQueryParams.size]);

  useEffect(() => {
    resetPage();
    setDisplayedItems(baseQueryParams.size);
  }, [
    baseQueryParams.keyword,
    baseQueryParams.newsletterId,
    baseQueryParams.sort,
    resetPage,
    baseQueryParams.size,
  ]);

  const totalElements = allMemos?.length ?? 0;
  const isLoadingOrHaveContent = isLoading || visibleMemos.length > 0;

  if (!isLoadingOrHaveContent)
    return <EmptyLetterCard title="메모한 뉴스레터가 없어요" />;

  return (
    <>
      <SummaryBar>
        <ResultsInfo>총 {totalElements}개의 메모</ResultsInfo>
        <Select
          options={[
            { value: 'DESC', label: '최신순' },
            { value: 'ASC', label: '오래된순' },
          ]}
          selectedValue={sortFilter}
          onSelectOption={onSortChange}
        />
      </SummaryBar>

      {visibleMemos.length > 0 ? (
        <>
          <MemoList>
            {visibleMemos.map((highlight) => (
              <li key={highlight.id}>
                <ReadOnlyMemoCard
                  id={highlight.id}
                  content={highlight.text}
                  memo={highlight.memo}
                  as="button"
                  onClick={() =>
                    navigate({ to: `/articles/${highlight.articleId}` })
                  }
                  newsletterName={highlight.newsletterName ?? ''}
                  newsletterImageUrl={highlight.newsletterImageUrl ?? ''}
                  articleTitle={highlight.articleTitle ?? ''}
                  createdAt={highlight.createdAt ?? ''}
                />
              </li>
            ))}
          </MemoList>

          {/* 무한 스크롤 로딩 트리거 */}
          <LoadMoreTrigger ref={loadMoreRef} />
          {hasNextPage && (
            <LoadingSpinner>더 많은 메모를 불러오는 중...</LoadingSpinner>
          )}
        </>
      ) : (
        <EmptyLetterCard title="메모한 뉴스레터가 없어요" />
      )}
    </>
  );
}

const SummaryBar = styled.div`
  width: 100%;
  margin-bottom: 20px;

  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
`;

const ResultsInfo = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const MemoList = styled.ul`
  width: 100%;

  display: grid;
  gap: 16px;

  grid-template-columns: 1fr;
`;

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
