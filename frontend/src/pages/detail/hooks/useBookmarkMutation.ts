import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteBookmark, postBookmark } from '@/apis/bookmark';
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
        queryKey: ['bookmarked', articleId],
      });
    },
  });

  const { mutate: removeBookmark } = useMutation({
    mutationKey: ['bookmarked', articleId],
    mutationFn: () => deleteBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['bookmarked', articleId],
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
