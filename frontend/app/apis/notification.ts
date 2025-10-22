import { fetcher } from './fetcher';

interface PostTokenParams {
  token: string;
}

interface PutTokenParams {
  token: string;
}

interface PutNotificationSettingsParams {
  memberId: number;
  deviceUuid: string;
  enabled: boolean;
}

export const postFCMToken = ({ token }: PostTokenParams) => {
  return fetcher.post({
    path: '/fcm/tokens',
    body: {
      token,
    },
  });
};

export const putFCMToken = ({ token }: PutTokenParams) => {
  return fetcher.put({
    path: '/fcm/tokens',
    body: {
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
    path: `/fcm/tokens/${memberId}/${deviceUuid}/notification`,
    body: {
      enabled,
    },
  });
};
