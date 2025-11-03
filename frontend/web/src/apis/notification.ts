import { fetcher } from '@bombom/shared/apis';

export interface GetNotificationSettingsParams {
  memberId: number;
  deviceUuid: string;
}

interface PutNotificationSettingsParams {
  memberId: number;
  deviceUuid: string;
  enabled: boolean;
}

export const getNotificationSettings = ({
  memberId,
  deviceUuid,
}: GetNotificationSettingsParams) => {
  return fetcher.get<boolean>({
    path: `/notifications/tokens/${memberId}/${deviceUuid}/settings/status`,
  });
};

export const putNotificationSettings = ({
  memberId,
  deviceUuid,
  enabled,
}: PutNotificationSettingsParams) => {
  return fetcher.put({
    path: `/notifications/tokens/${memberId}/${deviceUuid}/settings`,
    body: { enabled },
  });
};
