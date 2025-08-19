import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import Select from '@/components/Select/Select';
import ReadOnlyMemoCard from '@/pages/detail/components/MemoCard/ReadOnlyMemoCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

interface PCMemoContentProps {
  baseQueryParams: {
    sort: [string, 'DESC' | 'ASC'];
    keyword: string;
    size: number;
    newsletterId?: number;
    page: number;
  };
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  onPageChange: (page: number) => void;
  page: number;
  resetPage: () => void;
}

export default function PCMemoContent({
  baseQueryParams,
  sortFilter,
  onSortChange,
  onPageChange,
  page,
  resetPage,
}: PCMemoContentProps) {
  const navigate = useNavigate();

  const { data: highlights, isLoading } = useQuery({
    ...queries.highlights(baseQueryParams),
    enabled: true,
  });

  const totalPages = highlights?.totalPages ?? 1;

  const totalElements = highlights?.totalElements ?? 0;
  const isLoadingOrHaveContent =
    isLoading || (highlights?.content?.length ?? 0) > 0;

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

      {(highlights?.content?.length ?? 0) > 0 ? (
        <MemoList>
          {highlights?.content?.map((highlight) => (
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
      ) : (
        <EmptyLetterCard title="메모한 뉴스레터가 없어요" />
      )}

      {totalPages > 1 && (
        <Pagination
          currentPage={page + 1}
          totalPages={totalPages}
          onPageChange={(pageNum) => onPageChange(pageNum - 1)}
        />
      )}
    </>
  );
}

const SummaryBar = styled.div`
  width: 100%;
  margin-bottom: 24px;

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const ResultsInfo = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const MemoList = styled.ul`
  width: 100%;
  margin-bottom: 32px;

  display: grid;
  gap: 16px;

  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
`;
