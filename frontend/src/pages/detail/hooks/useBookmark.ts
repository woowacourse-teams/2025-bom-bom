import { useMutation, useQuery } from '@tanstack/react-query';
import { deleteBookmark, getBookmarked, postBookmark } from '@/apis/bookmark';
import { queryClient } from '@/main';

interface UseBookmarkParams {
  articleId: number;
}

const useBookmark = ({ articleId }: UseBookmarkParams) => {
  const { data: bookmarked } = useQuery({
    queryKey: ['bookmarked'],
    queryFn: () => getBookmarked({ articleId }),
  });
  const { mutate: addBookmark } = useMutation({
    mutationKey: ['bookmarked', String(articleId)],
    mutationFn: () => postBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['bookmarked', articleId],
      });
    },
  });
  const { mutate: removeBookmark } = useMutation({
    mutationKey: ['bookmarked', String(articleId)],
    mutationFn: () => deleteBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['bookmarked', articleId],
      });
    },
  });

  const onToggleBookmarkClick = () => {
    if (bookmarked) {
      removeBookmark();
    } else {
      addBookmark();
    }
  };

  return {
    bookmarked,
    onToggleBookmarkClick,
  };
};

export default useBookmark;
