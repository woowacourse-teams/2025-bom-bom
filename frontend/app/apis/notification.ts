import { fetcher } from '@bombom/shared/apis';

interface PutTokenParams {
  memberId: number;
  deviceUuid: string;
  token: string;
}

interface GetNotificationSettingsParams {
  memberId: number;
  deviceUuid: string;
}

interface GetNotificationSettingsResponse {
  enabled: boolean;
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

export const getNotificationSettings = ({
  memberId,
  deviceUuid,
}: GetNotificationSettingsParams) => {
  return fetcher.get<GetNotificationSettingsResponse>({
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
