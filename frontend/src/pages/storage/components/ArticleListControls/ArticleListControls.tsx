import styled from '@emotion/styled';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';
import TextSkeleton from '@/components/Skeleton/TextSkeleton';

interface ArticleListControlsProps {
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  totalElements: number | undefined;
  isLoading: boolean;
}

export default function ArticleListControls({
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
  totalElements,
  isLoading,
}: ArticleListControlsProps) {
  return (
    <Container>
      <SearchInput
        placeholder="뉴스레터 제목으로 검색하세요..."
        value={searchInput}
        onChange={onSearchChange}
      />
      <SummaryBar>
        {isLoading ? (
          <TextSkeleton width="80px" height="20px" />
        ) : (
          <SummaryText>총 {totalElements ?? 0}개</SummaryText>
        )}

        <Select
          options={[
            { value: 'DESC', label: '최신순' },
            { value: 'ASC', label: '오래된순' },
          ]}
          selectedValue={sortFilter}
          onSelectOption={onSortChange}
        />
      </SummaryBar>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const SummaryBar = styled.div`
  width: 100%;

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const SummaryText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
