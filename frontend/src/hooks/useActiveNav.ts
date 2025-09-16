import { useRouterState } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';
import { NavType } from '@/types/nav';

const navMap: Record<string, NavType> = {
  '/': 'today',
  '/storage': 'storage',
  '/recommend': 'recommend',
};

export const useActiveNav = (): NavType => {
  const location = useRouterState({
    select: (state) => state.location.pathname,
  });

  const previousNavRef = useRef<NavType>(null);

  useEffect(() => {
    if (navMap[location]) {
      previousNavRef.current = navMap[location];
    }
  }, [location]);

  return navMap[location] || previousNavRef.current;
};
