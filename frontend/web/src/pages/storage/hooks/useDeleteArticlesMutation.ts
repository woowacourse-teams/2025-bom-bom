import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteArticle } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { formatDate } from '@/utils/date';

export const useDeleteArticlesMutation = (
  deleteType: 'today' | 'article' | 'bookmark' = 'article',
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (articleIds: number[]) =>
      deleteArticle({ articleIds: articleIds }),
    onSuccess: () => {
      if (deleteType === 'today') {
        queryClient.invalidateQueries({
          queryKey: queries.articles({ date: formatDate(new Date(), '-') })
            .queryKey,
        });
      } else if (deleteType === 'article') {
        queryClient.invalidateQueries({
          queryKey: ['articles'],
        });
      } else {
        queryClient.invalidateQueries({
          queryKey: ['bookmarks'],
        });
      }
    },
  });
};
