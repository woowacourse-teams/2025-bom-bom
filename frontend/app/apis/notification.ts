import { fetcher } from '@bombom/shared/apis';

interface PostTokenParams {
  memberId: number;
  deviceUuid: string;
  token: string;
}

interface PutTokenParams {
  memberId: number;
  deviceUuid: string;
  token: string;
}

interface PutNotificationSettingsParams {
  memberId: number;
  deviceUuid: string;
  enabled: boolean;
}

export const putFCMToken = ({
  memberId,
  deviceUuid,
  token,
}: PutTokenParams) => {
  return fetcher.put({
    path: '/notifications/tokens',
    body: {
      memberId,
      deviceUuid,
      token,
    },
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
