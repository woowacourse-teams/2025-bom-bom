import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchWeeklyReadingGoal } from '@/apis/members';
import { queries } from '@/apis/queries';
import { toast } from '@/components/Toast/utils/toastActions';

type UpdateWeeklyGoalVariables = Parameters<typeof patchWeeklyReadingGoal>[0];

type UseUpdateWeeklyGoalMutationOptions = {
  onSuccess?: () => void;
};

const useUpdateWeeklyGoalMutation = (
  options?: UseUpdateWeeklyGoalMutationOptions,
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (variables: UpdateWeeklyGoalVariables) =>
      patchWeeklyReadingGoal(variables),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: queries.readingStatus().queryKey,
      });
      options?.onSuccess?.();
      toast.success('목표 수정에 성공했습니다.');
    },
    onError: () => {
      toast.error('목표 수정에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

export default useUpdateWeeklyGoalMutation;
