import { fetcher } from './fetcher';

export type GetServerStatusResponse = 'UP' | 'DOWN';

export const getServerStatus = async () => {
  return (await fetcher.get)<GetServerStatusResponse>({
    path: '/actuator/health',
  });
};
