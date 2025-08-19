import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import SearchInput from '@/components/SearchInput/SearchInput';
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
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  onPageChange: (page: number) => void;
  page: number;
  resetPage: () => void;
}

export default function PCMemoContent({
  baseQueryParams,
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
  onPageChange,
  page,
  resetPage,
}: PCMemoContentProps) {
  const navigate = useNavigate();

  const { data: highlights, isLoading } = useQuery({
    ...queries.highlights(),
    enabled: true,
  });

  const filteredHighlights =
    highlights?.content?.filter((highlight) => {
      const matchesNewsletter =
        !baseQueryParams.newsletterId ||
        highlight.id === baseQueryParams.newsletterId;

      const matchesKeyword =
        !baseQueryParams.keyword ||
        highlight.text
          .toLowerCase()
          .includes(baseQueryParams.keyword.toLowerCase()) ||
        highlight.memo
          ?.toLowerCase()
          .includes(baseQueryParams.keyword.toLowerCase()) ||
        highlight.newsletterName
          ?.toLowerCase()
          .includes(baseQueryParams.keyword.toLowerCase()) ||
        highlight.articleTitle
          ?.toLowerCase()
          .includes(baseQueryParams.keyword.toLowerCase());

      return matchesNewsletter && matchesKeyword;
    }) || [];

  const sortedHighlights = filteredHighlights.sort((a, b) => {
    const dateA = new Date(a.createdAt ?? '').getTime();
    const dateB = new Date(b.createdAt ?? '').getTime();

    return sortFilter === 'DESC' ? dateB - dateA : dateA - dateB;
  });

  const totalPages = Math.ceil(sortedHighlights.length / baseQueryParams.size);
  const startIndex = page * baseQueryParams.size;
  const endIndex = startIndex + baseQueryParams.size;
  const currentPageHighlights = sortedHighlights.slice(startIndex, endIndex);

  const totalElements = sortedHighlights.length;
  const isLoadingOrHaveContent = isLoading || currentPageHighlights.length > 0;

  if (!isLoadingOrHaveContent && searchInput === '')
    return <EmptyLetterCard title="메모한 뉴스레터가 없어요" />;

  return (
    <>
      <ControlsWrapper>
        <SearchInput
          value={searchInput}
          onChange={onSearchChange}
          placeholder="메모 내용이나 뉴스레터 제목으로 검색하세요"
        />
      </ControlsWrapper>

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

      {currentPageHighlights.length > 0 ? (
        <MemoList>
          {currentPageHighlights.map((highlight) => (
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
        <EmptyLetterCard title="검색 결과가 없어요" />
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

const ControlsWrapper = styled.div`
  margin-bottom: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

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
