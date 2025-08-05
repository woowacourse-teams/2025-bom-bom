import { useMutation } from '@tanstack/react-query';
import { deleteBookmark, postBookmark } from '@/apis/bookmark';
import { queryClient } from '@/main';

interface UseBookmarkMutationParams {
  articleId: number;
}

const useBookmarkMutation = ({ articleId }: UseBookmarkMutationParams) => {
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

  const onToggleBookmarkClick = (bookmarked: boolean) => {
    if (bookmarked) {
      removeBookmark();
    } else {
      addBookmark();
    }
  };

  return {
    onToggleBookmarkClick,
  };
};

export default useBookmarkMutation;
