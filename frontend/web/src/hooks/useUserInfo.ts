import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import { queries } from '@/apis/queries';

export const useUserInfo = () => {
  const { data: userInfo, isLoading } = useQuery(queries.userProfile());

  const isLoggedIn = useMemo(() => Boolean(userInfo), [userInfo]);

  return { userInfo, isLoggedIn, isLoading };
};
