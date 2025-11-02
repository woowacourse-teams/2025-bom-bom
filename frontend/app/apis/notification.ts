import { fetcher } from '@bombom/shared/apis';

interface PostTokenParams {
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
