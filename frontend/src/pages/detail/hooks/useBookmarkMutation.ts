import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteBookmark, postBookmark } from '@/apis/bookmark';
import { queries } from '@/apis/queries';
import { useDebounce } from '@/hooks/useDebounce';

interface UseBookmarkMutationParams {
  articleId: number;
}

const useBookmarkMutation = ({ articleId }: UseBookmarkMutationParams) => {
  const queryClient = useQueryClient();

  const { mutate: addBookmark } = useMutation({
    mutationKey: ['bookmarked', articleId],
    mutationFn: () => postBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.bookmarkStatusByArticleId({ articleId }).queryKey,
      });
    },
  });

  const { mutate: removeBookmark } = useMutation({
    mutationFn: () => deleteBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.bookmarkStatusByArticleId({ articleId }).queryKey,
      });
    },
  });

  const toggleBookmark = useDebounce((bookmarked: boolean) => {
    if (bookmarked) {
      removeBookmark();
    } else {
      addBookmark();
    }
  }, 500);

  return { toggleBookmark };
};

export default useBookmarkMutation;
