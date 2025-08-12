import { useQuery } from '@tanstack/react-query';
import { useCallback, useEffect, useState } from 'react';
import useAddBookmarkMutation from './useAddBookmarkMutation';
import useRemoveBookmarkMutation from './useRemoveBookmarkMutation';
import { queries } from '@/apis/queries';
import { useDebounce } from '@/hooks/useDebounce';

interface UseArticleBookmarkParams {
  articleId: number;
}

export const useArticleBookmark = ({ articleId }: UseArticleBookmarkParams) => {
  const { data: bookmarked } = useQuery(
    queries.bookmarkStatusByArticleId({ articleId }),
  );
  const [isBookmarked, setIsBookmarked] = useState(false);
  const { mutate: addBookmark } = useAddBookmarkMutation({ articleId });
  const { mutate: removeBookmark } = useRemoveBookmarkMutation({ articleId });

  const debouncedSyncBookmark = useDebounce(() => {
    if (isBookmarked) {
      addBookmark();
    } else {
      removeBookmark();
    }
  }, 500);

  const toggleBookmark = useCallback(() => {
    setIsBookmarked((prev) => !prev);
    debouncedSyncBookmark();
  }, [debouncedSyncBookmark]);

  useEffect(() => {
    setIsBookmarked(bookmarked?.bookmarkStatus ?? false);
  }, [bookmarked?.bookmarkStatus]);

  return {
    isBookmarked,
    toggleBookmark,
  };
};
