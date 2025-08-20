import { useMutation } from '@tanstack/react-query';
import { patchGuideArticleRead } from '@/apis/guide';

interface UseGuideAsReadMutationParams {
  onSuccess: () => void;
}

const useGuideAsReadMutation = ({
  onSuccess,
}: UseGuideAsReadMutationParams) => {
  return useMutation({
    mutationFn: patchGuideArticleRead,
    onSuccess,
  });
};

export default useGuideAsReadMutation;
