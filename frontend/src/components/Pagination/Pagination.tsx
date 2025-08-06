import styled from '@emotion/styled';
import ChevronLeftIcon from '#/assets/chevron-left.svg';
import ChevronRightIcon from '#/assets/chevron-right.svg';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  const getPageNumbers = () => {
    const pages: (number | string)[] = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        for (let i = 0; i < 4; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages - 1);
      } else if (currentPage >= totalPages - 2) {
        pages.push(0);
        pages.push('...');
        for (let i = totalPages - 4; i < totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(0);
        pages.push('...');
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages - 1);
      }
    }

    return pages;
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  if (totalPages <= 1) return null;

  return (
    <Container>
      <NavigationButton
        onClick={handlePrevPage}
        disabled={currentPage === 0}
        aria-label="이전 페이지"
      >
        <ChevronLeftIcon />
      </NavigationButton>

      <PageNumbers>
        {getPageNumbers().map((page, index) => (
          <PageNumber
            key={index}
            isCurrent={page === currentPage}
            isDisabled={page === '...'}
            onClick={() => typeof page === 'number' && onPageChange(page)}
          >
            {page === '...' ? '...' : (page as number) + 1}
          </PageNumber>
        ))}
      </PageNumbers>

      <NavigationButton
        onClick={handleNextPage}
        disabled={currentPage === totalPages - 1}
        aria-label="다음 페이지"
      >
        <ChevronRightIcon />
      </NavigationButton>
    </Container>
  );
}

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
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};

  cursor: pointer;
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    background-color: ${({ theme }) => theme.colors.background};
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  svg {
    width: 16px;
    height: 16px;

    color: ${({ theme }) => theme.colors.textPrimary};
  }
`;

const PageNumbers = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;

const PageNumber = styled.button<{
  isCurrent: boolean;
  isDisabled: boolean;
}>`
  height: 40px;
  min-width: 40px;
  padding: 0 12px;
  border: 1px solid
    ${({ theme, isCurrent }) =>
      isCurrent ? theme.colors.primary : theme.colors.border};
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

  &:hover:not(:disabled) {
    background-color: ${({ theme, isCurrent }) =>
      isCurrent ? theme.colors.primary : theme.colors.background};
  }

  &:disabled {
    cursor: not-allowed;
  }
`;
