import styled from '@emotion/styled';
import Checkbox from '@/components/Checkbox/Checkbox';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';

interface ArticleListControlsProps {
  searchInput: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  editMode: boolean;
  onSelectDeleteButtonClick: () => void;
  onDeleteButtonClick: () => void;
  allChecked: boolean;
  onAllSelectClick: () => void;
}

export default function ArticleListControls({
  searchInput,
  onSearchChange,
  sortFilter,
  onSortChange,
  editMode,
  onSelectDeleteButtonClick,
  allChecked,
  onAllSelectClick,
}: ArticleListControlsProps) {
  return (
    <Container>
      <SearchInput
        placeholder="뉴스레터 제목으로 검색하세요..."
        value={searchInput}
        onChange={onSearchChange}
      />
      <SummaryBar>
        {editMode ? (
          <Checkbox id="all" checked={allChecked} onChange={onAllSelectClick} />
        ) : (
          <DeleteButton onClick={onSelectDeleteButtonClick}>
            선택 삭제
          </DeleteButton>
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

const DeleteButton = styled.button`
  padding-left: 8px;

  display: flex;
  gap: 4px;
  align-items: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  &:hover {
    text-decoration: underline;
    transition: all 0.2s ease-in-out;
  }
`;
