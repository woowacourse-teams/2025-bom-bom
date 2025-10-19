import { useState, useMemo, useCallback } from 'react';
import type { Article } from '@/types/articles';

export function useSelectedDeleteIds(articleList: Article[]) {
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const isAllSelected = useMemo(
    () => selectedIds.length === articleList.length && articleList.length > 0,
    [selectedIds, articleList],
  );

  const toggleSelectAll = useCallback(() => {
    if (isAllSelected) {
      setSelectedIds([]);
    } else {
      setSelectedIds(articleList.map((article) => article.articleId));
    }
  }, [articleList, isAllSelected]);

  const toggleSelect = useCallback((id: number) => {
    setSelectedIds((prev) =>
      prev.includes(id)
        ? prev.filter((selectedId) => selectedId !== id)
        : [...prev, id],
    );
  }, []);

  const clearSelection = useCallback(() => {
    setSelectedIds([]);
  }, []);

  return {
    selectedIds,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  };
}
