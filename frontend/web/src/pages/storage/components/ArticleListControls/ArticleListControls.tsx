import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { type ChangeEvent } from 'react';
import Checkbox from '@/components/Checkbox/Checkbox';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';
import CancelIcon from '#/assets/svg/close.svg';
import DeleteIcon from '#/assets/svg/delete.svg';

interface ArticleListControlsProps {
  searchInput: string;
  onSearchChange: (e: ChangeEvent<HTMLInputElement>) => void;
  sortFilter: 'DESC' | 'ASC';
  onSortChange: (value: 'DESC' | 'ASC') => void;
  editMode: boolean;
  onSelectDeleteButtonClick: () => void;
  onSelectCancelButtonClick: () => void;
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
  onSelectCancelButtonClick,
  onDeleteButtonClick,
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
          <DeleteWrapper>
            <Checkbox
              id="all"
              checked={allChecked}
              onChange={onAllSelectClick}
            />
            <DeleteIcon
              fill={theme.colors.error}
              onClick={onDeleteButtonClick}
            />
            <CancelIcon
              fill={theme.colors.black}
              onClick={onSelectCancelButtonClick}
            />
          </DeleteWrapper>
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

const DeleteWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
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
