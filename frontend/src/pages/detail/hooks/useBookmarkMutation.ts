import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteBookmark, postBookmark } from '@/apis/bookmark';

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

  const onToggleBookmark = (bookmarked: boolean) => {
    if (bookmarked) {
      removeBookmark();
    } else {
      addBookmark();
    }
  };

  return {
    onToggleBookmark,
  };
};

export default useBookmarkMutation;
