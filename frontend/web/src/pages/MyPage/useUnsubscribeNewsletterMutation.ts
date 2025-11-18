import { useMutation, useQueryClient } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import { toast } from '@/components/Toast/utils/toastActions';

const unsubscribeNewsletter = async ({
  newsletterId,
}: {
  newsletterId: number;
}) => {
  console.log('구독 해지', newsletterId);
};

export const useUnsubscribeNewsletterMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: unsubscribeNewsletter,
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: queries.myNewsletters().queryKey,
      });
      toast.success('뉴스레터 구독을 해지했습니다.');
    },
    onError: () => {
      toast.error('구독 해지에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
