import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { GetHighlightsParams } from '@/apis/highlight';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import TextSkeleton from '@/components/Skeleton/TextSkeleton';
import ReadOnlyMemoCard from '@/pages/detail/components/MemoCard/ReadOnlyMemoCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

interface PCMemoContentProps {
  baseQueryParams: GetHighlightsParams;
  onPageChange: (page: number) => void;
  page: number;
  resetPage: () => void;
}

export default function PCMemoContent({
  baseQueryParams,
  onPageChange,
  page,
}: PCMemoContentProps) {
  const navigate = useNavigate();

  const { data: highlights, isLoading } = useQuery({
    ...queries.highlights({
      ...baseQueryParams,
      newsletterId: baseQueryParams.newsletterId || undefined,
    }),
    enabled: true,
  });

  const totalPages = highlights?.totalPages ?? 1;
  const totalElements = highlights?.totalElements ?? 0;

  const highlightContent = highlights?.content ?? [];

  const haveNoContent = !isLoading && highlightContent.length === 0;

  if (haveNoContent)
    return <EmptyLetterCard title="메모한 뉴스레터가 없어요" />;

  return (
    <>
      <SummaryBar>
        {isLoading ? (
          <TextSkeleton width="80px" height="20px" />
        ) : (
          <ResultsInfo>총 {totalElements}개의 메모</ResultsInfo>
        )}
      </SummaryBar>

      <MemoList>
        {highlightContent.map((highlight) => (
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
