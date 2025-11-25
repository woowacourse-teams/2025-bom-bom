import { useMutation, useQueryClient } from '@tanstack/react-query';
import { postNewsletterUnsubscribe } from '@/apis/members';
import { queries } from '@/apis/queries';
import { toast } from '@/components/Toast/utils/toastActions';
import type { PostNewsletterUnsubscribeParams } from '@/apis/members';

export const useUnsubscribeNewsletterMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: PostNewsletterUnsubscribeParams) =>
      postNewsletterUnsubscribe(params),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.myNewsletters().queryKey,
      });
      toast.success('뉴스레터 구독을 해지했습니다.');
    },
    onError: () => {
      toast.error('구독 해지에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
