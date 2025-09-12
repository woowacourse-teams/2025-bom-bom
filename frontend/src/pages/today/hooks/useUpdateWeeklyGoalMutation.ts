import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchWeeklyReadingGoal } from '@/apis/members';
import { queries } from '@/apis/queries';

type UpdateWeeklyGoalVariables = Parameters<typeof patchWeeklyReadingGoal>[0];

type UseUpdateWeeklyGoalMutationOptions = {
  onSuccess?: () => void;
  onError?: (error: unknown) => void;
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
    },
    onError: (error) => {
      options?.onError?.(error);
    },
  });
};

export default useUpdateWeeklyGoalMutation;
