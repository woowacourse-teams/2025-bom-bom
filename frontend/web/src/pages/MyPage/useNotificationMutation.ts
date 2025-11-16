import { ApiError } from '@bombom/shared/apis';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { putNotificationSettings } from '@/apis/notification';
import { queries } from '@/apis/queries';
import { toast } from '@/components/Toast/utils/toastActions';
import { sendMessageToRN } from '@/libs/webview/webview.utils';

interface useNotificationMutationParams {
  memberId: number;
  deviceUuid: string;
}

const useNotificationMutation = ({
  memberId,
  deviceUuid,
}: useNotificationMutationParams) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (enabled: boolean) =>
      putNotificationSettings({ memberId, deviceUuid, enabled }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.notificationStatus({
          memberId,
          deviceUuid,
        }).queryKey,
      });
    },
    onError: (error, enabled) => {
      if (enabled && error instanceof ApiError && error.status === 404) {
        sendMessageToRN({
          type: 'REGISTER_FCM_TOKEN',
          payload: { memberId },
        });
        return;
      }

      toast.error('알림 설정 변경에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

export default useNotificationMutation;
