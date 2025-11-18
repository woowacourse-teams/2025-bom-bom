import { fetcher } from '@bombom/shared/apis';

interface PutTokenParams {
  memberId: number;
  deviceUuid: string;
  token: string;
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
