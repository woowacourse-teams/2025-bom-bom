import { ELLIPSIS } from './Pagination.constants';

/**
 * 페이지네이션에서 표시할 페이지 번호들을 생성합니다.
 *
 * @param currentPage - 현재 페이지 번호 (0부터 시작)
 * @param totalPages - 총 페이지 수
 * @returns 표시할 페이지 번호 배열 (숫자 또는 '...' 문자열)
 */
export const getPageNumbers = (
  currentPage: number,
  totalPages: number,
): (number | string)[] => {
  const maxVisiblePages = 7;

  // 모든 페이지를 표시할 수 있는 경우
  if (totalPages <= maxVisiblePages) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  // 첫 번째 페이지 근처인 경우
  if (currentPage <= 3) {
    return [
      ...Array.from({ length: 5 }, (_, i) => i + 1),
      ELLIPSIS,
      totalPages,
    ];
  }

  // 마지막 페이지 근처인 경우
  if (currentPage >= totalPages - 2) {
    return [
      1,
      ELLIPSIS,
      ...Array.from({ length: 5 }, (_, i) => totalPages - 4 + i + 1),
    ];
  }

  // 중간 페이지인 경우
  return [
    1,
    ELLIPSIS,
    currentPage - 1,
    currentPage,
    currentPage + 1,
    ELLIPSIS,
    totalPages,
  ];
};

/**
 * 페이지 번호가 유효한지 확인합니다.
 *
 * @param page - 확인할 페이지 번호
 * @param totalPages - 총 페이지 수
 * @returns 유효한 페이지 번호인지 여부
 */
export const isValidPage = (page: number, totalPages: number): boolean => {
  return page >= 0 && page < totalPages;
};

/**
 * 이전 페이지로 이동할 수 있는지 확인합니다.
 *
 * @param currentPage - 현재 페이지 번호
 * @returns 이전 페이지로 이동 가능한지 여부
 */
export const canGoToPreviousPage = (currentPage: number): boolean => {
  return currentPage > 0;
};

/**
 * 다음 페이지로 이동할 수 있는지 확인합니다.
 *
 * @param currentPage - 현재 페이지 번호
 * @param totalPages - 총 페이지 수
 * @returns 다음 페이지로 이동 가능한지 여부
 */
export const canGoToNextPage = (
  currentPage: number,
  totalPages: number,
): boolean => {
  return currentPage < totalPages;
};

/**
 * 페이지네이션을 표시해야 하는지 확인합니다.
 *
 * @param totalPages - 총 페이지 수
 * @returns 페이지네이션을 표시해야 하는지 여부
 */
export const shouldShowPagination = (totalPages: number): boolean => {
  return totalPages > 1;
};
