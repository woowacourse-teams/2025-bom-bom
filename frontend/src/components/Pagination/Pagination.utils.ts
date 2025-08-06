export const getPageNumbers = (currentPage: number, totalPages: number) => {
  const maxVisiblePages = 5;

  // 모든 페이지를 표시할 수 있는 경우
  if (totalPages <= maxVisiblePages) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  // 첫 번째 페이지 근처인 경우
  if (currentPage <= 3) {
    return [...Array.from({ length: 4 }, (_, i) => i), '...', totalPages - 1];
  }

  // 마지막 페이지 근처인 경우
  if (currentPage >= totalPages - 2) {
    return [
      0,
      '...',
      ...Array.from({ length: 4 }, (_, i) => totalPages - 4 + i),
    ];
  }

  // 중간 페이지인 경우
  return [
    0,
    '...',
    currentPage - 1,
    currentPage,
    currentPage + 1,
    '...',
    totalPages - 1,
  ];
};
