import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getPet = async () => {
  return await fetcher.get<components['schemas']['PetResponse']>({
    path: '/members/me/pet',
  });
};

export const postPetAttendance = async () => {
  return await fetcher.post({
    path: '/members/me/pet/attendance',
  });
};
