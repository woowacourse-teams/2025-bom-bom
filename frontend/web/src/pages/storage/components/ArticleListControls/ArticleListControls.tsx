import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useQueryState } from 'nuqs';
import { useEffect, useState, type ChangeEvent } from 'react';
import Checkbox from '@/components/Checkbox/Checkbox';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import type { Sort } from './ArticleListControls.types';
import CancelIcon from '#/assets/svg/close.svg';
import DeleteIcon from '#/assets/svg/delete.svg';

interface ArticleListControlsProps {
  editMode: boolean;
  onSelectDeleteButtonClick: () => void;
  onSelectCancelButtonClick: () => void;
  onDeleteButtonClick: () => void;
  allChecked: boolean;
  onAllSelectClick: () => void;
}

export default function ArticleListControls({
  editMode,
  onSelectDeleteButtonClick,
  onSelectCancelButtonClick,
  onDeleteButtonClick,
  allChecked,
  onAllSelectClick,
}: ArticleListControlsProps) {
  const [search, setSearch] = useState('');
  const [, setSearchParam] = useQueryState('search', {
    defaultValue: '',
  });
  const [sort, setSort] = useQueryState('sort', { defaultValue: 'DESC' });
  const debouncedSearchInput = useDebouncedValue(search, 500);

  const handleSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  };

  const handleSortChange = (value: 'DESC' | 'ASC') => {
    setSort(value);
  };

  useEffect(() => {
    setSearchParam(debouncedSearchInput);
  }, [debouncedSearchInput, setSearchParam]);

  return (
    <Container>
      <SearchInput
        placeholder="뉴스레터 제목으로 검색하세요..."
        value={search}
        onChange={handleSearchChange}
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
          selectedValue={sort as Sort}
          onSelectOption={handleSortChange}
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
