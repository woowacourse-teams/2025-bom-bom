import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useQueryState } from 'nuqs';
import { useEffect, useState, type ChangeEvent } from 'react';
import ArticleDeleteModal from '../ArticleDeleteModal/ArticleDeleteModal';
import Checkbox from '@/components/Checkbox/Checkbox';
import useModal from '@/components/Modal/useModal';
import SearchInput from '@/components/SearchInput/SearchInput';
import Select from '@/components/Select/Select';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import type { Sort } from './ArticleListControls.types';
import CancelIcon from '#/assets/svg/close.svg';
import DeleteIcon from '#/assets/svg/delete.svg';

interface ArticleListControlsProps {
  editMode: boolean;
  onEnterEditMode: () => void;
  onExitEditMode: () => void;
  onDeleteSelected: () => void;
  checkedCount: number;
  isAllSelected: boolean;
  onToggleSelectAll: () => void;
  hasBookmarkedArticles?: boolean;
}

const ArticleListControls = ({
  editMode,
  onEnterEditMode,
  onExitEditMode,
  onDeleteSelected,
  checkedCount,
  isAllSelected,
  onToggleSelectAll,
  hasBookmarkedArticles = false,
}: ArticleListControlsProps) => {
  const [search, setSearch] = useState('');
  const [, setSearchParam] = useQueryState('search', {
    defaultValue: '',
  });
  const [sort, setSort] = useQueryState('sort', { defaultValue: 'DESC' });
  const debouncedSearchInput = useDebouncedValue(search, 500);
  const { modalRef, isOpen, openModal, closeModal } = useModal();

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
              checked={isAllSelected}
              onChange={onToggleSelectAll}
            />
            <DeleteCount>{checkedCount}개 선택됨</DeleteCount>
            <HorizontalDivider />
            <DeleteIconButton
              disabled={checkedCount === 0}
              onClick={() => {
                if (checkedCount === 0) return;

                openModal();
              }}
            />
            <CancelIcon fill={theme.colors.black} onClick={onExitEditMode} />
          </DeleteWrapper>
        ) : (
          <TextButton onClick={onEnterEditMode}>선택 삭제</TextButton>
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
      <ArticleDeleteModal
        modalRef={modalRef}
        isOpen={isOpen}
        closeModal={closeModal}
        onDelete={onDeleteSelected}
        hasBookmarkedArticles={hasBookmarkedArticles}
      />
    </Container>
  );
};

export default ArticleListControls;

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

const DeleteCount = styled.p`
  width: 68px;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const HorizontalDivider = styled.div`
  width: 2px;
  height: 16px;

  background-color: ${({ theme }) => theme.colors.stroke};
`;

const TextButton = styled.button`
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

const DeleteIconButton = styled(DeleteIcon)<{ disabled: boolean }>`
  fill: ${({ theme, disabled }) =>
    disabled ? theme.colors.disabledBackground : theme.colors.error};
`;
