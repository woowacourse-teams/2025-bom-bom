import { fetcher } from './fetcher';

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

export const postFCMToken = ({
  memberId,
  deviceUuid,
  token,
}: PostTokenParams) => {
  return fetcher.post({
    path: '/notifications/tokens',
    body: {
      memberId,
      deviceUuid,
      token,
    },
  });
};

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
