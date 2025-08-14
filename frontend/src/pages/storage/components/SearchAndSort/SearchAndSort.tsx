import styled from '@emotion/styled';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';

interface SearchAndSortProps {
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  totalElements: number | undefined;
}

export default function SearchAndSort({
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
  totalElements,
}: SearchAndSortProps) {
  return (
    <Container>
      <SearchInput
        placeholder="뉴스레터 제목으로 검색하세요..."
        value={searchInput}
        onChange={onSearchChange}
      />
      <SummaryBar>
        <SummaryText>총 {totalElements ?? 0}개</SummaryText>
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
  gap: 20px;
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
