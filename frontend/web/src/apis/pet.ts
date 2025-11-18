import { fetcher } from '@bombom/shared/apis';
import type { components } from '@/types/openapi';

export type GetPetResponse = components['schemas']['PetResponse'];

export const getPet = async () => {
  return await fetcher.get<GetPetResponse>({
    path: '/members/me/pet',
  });
};

export const postPetAttendance = async () => {
  return await fetcher.post({
    path: '/members/me/pet/attendance',
  });
};
