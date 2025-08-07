import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteBookmark, postBookmark } from '@/apis/bookmark';
import { useDebounce } from '@/hooks/useDebounce';

interface UseBookmarkMutationParams {
  articleId: number;
}

const useBookmarkMutation = ({ articleId }: UseBookmarkMutationParams) => {
  const queryClient = useQueryClient();

  const debounceInvalidateQueries = useDebounce(() => {
    queryClient.invalidateQueries({ queryKey: ['bookmarked', articleId] });
  }, 300);

  const { mutate: addBookmark } = useMutation({
    mutationFn: () => postBookmark({ articleId }),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['bookmarked', articleId] });
      const previousBookmark = queryClient.getQueryData<boolean>([
        'bookmarked',
        articleId,
      ]);
      queryClient.setQueryData(['bookmarked', articleId], {
        bookmarkStatus: true,
      });
      return { previousBookmark };
    },
    onError: (err, newBookmark, context) => {
      queryClient.setQueryData(
        ['bookmarked', articleId],
        context?.previousBookmark,
      );
    },
    onSettled: () => debounceInvalidateQueries(),
  });

  const { mutate: removeBookmark } = useMutation({
    mutationFn: () => deleteBookmark({ articleId }),
    onMutate: async () => {
      await queryClient.cancelQueries({
        queryKey: ['bookmarked', articleId],
      });
      const previousBookmark = queryClient.getQueryData<boolean>([
        'bookmarked',
        articleId,
      ]);
      queryClient.setQueryData(['bookmarked', articleId], {
        bookmarkStatus: false,
      });
      return { previousBookmark };
    },
    onError: (err, newBookmark, context) => {
      queryClient.setQueryData(
        ['bookmarked', articleId],
        context?.previousBookmark,
      );
    },
    onSettled: () => debounceInvalidateQueries(),
  });

  const toggleBookmark = (bookmarked: boolean) => {
    if (bookmarked) {
      removeBookmark();
    } else {
      addBookmark();
    }
  };

  return { toggleBookmark };
};

export default useBookmarkMutation;
