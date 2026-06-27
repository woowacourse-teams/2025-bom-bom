import { useMutation, useQueryClient } from '@tanstack/react-query';
import { postBookmark } from '@/apis/bookmark';
import { queries } from '@/apis/queries';

const useAddBookmarkMutation = ({ articleId }: { articleId: number }) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => postBookmark({ articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.articleBookmarkStatus({ articleId }).queryKey,
      });
    },
  });
};

export default useAddBookmarkMutation;
