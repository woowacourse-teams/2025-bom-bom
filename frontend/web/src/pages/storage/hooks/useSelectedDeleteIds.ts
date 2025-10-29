import { useState, useMemo, useCallback } from 'react';
import type { Article } from '@/types/articles';

export function useSelectedDeleteIds(articleList: Article[]) {
  const [selectedList, setSelectedList] = useState<Article[]>([]);

  const isAllSelected = useMemo(
    () => selectedList.length === articleList.length && articleList.length > 0,
    [selectedList, articleList],
  );

  const selectedIds = useMemo(
    () => selectedList.map((article) => article.articleId),
    [selectedList],
  );

  const hasBookmarkedArticles = useMemo(
    () => selectedList.some((article) => article.isBookmarked),
    [selectedList],
  );

  const toggleSelectAll = useCallback(() => {
    if (isAllSelected) {
      setSelectedList([]);
    } else {
      setSelectedList(articleList);
    }
  }, [articleList, isAllSelected]);

  const toggleSelect = useCallback(
    (id: number) => {
      setSelectedList((prev) => {
        const article = articleList.find((a) => a.articleId === id);
        if (!article) return prev;

        return prev.some((a) => a.articleId === id)
          ? prev.filter((a) => a.articleId !== id)
          : [...prev, article];
      });
    },
    [articleList],
  );

  const clearSelection = useCallback(() => {
    setSelectedList([]);
  }, []);

  return {
    selectedIds,
    hasBookmarkedArticles,
    isAllSelected,
    toggleSelectAll,
    toggleSelect,
    clearSelection,
  };
}
