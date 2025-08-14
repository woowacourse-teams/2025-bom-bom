import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteBookmark } from '@/apis/bookmark';
import { queries } from '@/apis/queries';

const useRemoveBookmarkMutation = ({ articleId }: { articleId: number }) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => deleteBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.articleBookmarkStatus({ articleId }).queryKey,
      });
    },
  });
};

export default useRemoveBookmarkMutation;
