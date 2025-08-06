import styled from '@emotion/styled';
import { ELLIPSIS } from './Pagination.constants';
import {
  getPageNumbers,
  canGoToPreviousPage,
  canGoToNextPage,
  shouldShowPagination,
} from './Pagination.utils';
import ChevronLeftIcon from '#/assets/chevron-left.svg';
import ChevronRightIcon from '#/assets/chevron-right.svg';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const Pagination = ({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) => {
  const pageNumbers = getPageNumbers(currentPage, totalPages);

  const handlePrevPage = () => {
    if (canGoToPreviousPage(currentPage)) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (canGoToNextPage(currentPage, totalPages)) {
      onPageChange(currentPage + 1);
    }
  };

  if (!shouldShowPagination(totalPages)) return null;

  return (
    <Container>
      <NavigationButton
        onClick={handlePrevPage}
        disabled={!canGoToPreviousPage(currentPage)}
        aria-label="이전 페이지"
      >
        <ChevronLeftIcon />
      </NavigationButton>

      <PageNumberButtonWrapper>
        {pageNumbers.map((page, index) => (
          <PageNumberButton
            key={index}
            isCurrent={page === currentPage}
            isDisabled={page === ELLIPSIS}
            onClick={() => typeof page === 'number' && onPageChange(page)}
          >
            {page === ELLIPSIS ? ELLIPSIS : page}
          </PageNumberButton>
        ))}
      </PageNumberButtonWrapper>

      <NavigationButton
        onClick={handleNextPage}
        disabled={!canGoToNextPage(currentPage, totalPages)}
        aria-label="다음 페이지"
      >
        <ChevronRightIcon />
      </NavigationButton>
    </Container>
  );
};

export default Pagination;

const Container = styled.div`
  margin-top: 32px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
`;

const NavigationButton = styled.button`
  width: 40px;
  height: 40px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};

  cursor: pointer;
  transition: all 0.2s ease;

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  &:hover:not(:disabled) {
    background-color: ${({ theme }) => theme.colors.disabledBackground};
  }

  svg {
    width: 16px;
    height: 16px;

    color: ${({ theme }) => theme.colors.textPrimary};
  }
`;

const PageNumberButtonWrapper = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;

const PageNumberButton = styled.button<{
  isCurrent: boolean;
  isDisabled: boolean;
}>`
  height: 40px;
  min-width: 40px;
  padding: 0 12px;
  border: 1px solid
    ${({ theme, isCurrent }) =>
      isCurrent ? theme.colors.primary : theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme, isCurrent }) =>
    isCurrent ? theme.colors.primary : theme.colors.white};
  color: ${({ theme, isCurrent }) =>
    isCurrent ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};

  cursor: ${({ isDisabled }) => (isDisabled ? 'default' : 'pointer')};
  transition: all 0.2s ease;

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  &:hover:not(:disabled) {
    background-color: ${({ theme, isCurrent }) =>
      isCurrent ? theme.colors.primary : theme.colors.disabledBackground};
  }
`;
