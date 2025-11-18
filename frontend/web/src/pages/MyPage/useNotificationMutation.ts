import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useRef } from 'react';
import { putNotificationSettings } from '@/apis/notification';
import { queries } from '@/apis/queries';
import { toast } from '@/components/Toast/utils/toastActions';
import { sendMessageToRN } from '@/libs/webview/webview.utils';
import type { ApiError } from '@bombom/shared/apis';

interface useNotificationMutationParams {
  memberId: number;
  deviceUuid: string;
}

const useNotificationMutation = ({
  memberId,
  deviceUuid,
}: useNotificationMutationParams) => {
  const queryClient = useQueryClient();
  const lastEnabledRef = useRef<boolean | null>(null);

  return useMutation<
    Awaited<ReturnType<typeof putNotificationSettings>>,
    ApiError,
    boolean
  >({
    mutationFn: (enabled: boolean) => {
      lastEnabledRef.current = enabled;
      return putNotificationSettings({ memberId, deviceUuid, enabled });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.notificationStatus({
          memberId,
          deviceUuid,
        }).queryKey,
      });
    },
    onError: () => {
      toast.error('알림 설정 변경에 실패했습니다. 다시 시도해주세요.');
    },
    retry: (failureCount, error) => {
      const shouldRetry =
        Boolean(lastEnabledRef.current) &&
        error.status === 404 &&
        failureCount < 1;

      if (shouldRetry) {
        sendMessageToRN({
          type: 'REGISTER_FCM_TOKEN',
          payload: { memberId },
        });
      }

      return shouldRetry;
    },
  });
};

export default useNotificationMutation;
