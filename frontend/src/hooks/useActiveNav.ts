import { useRouterState } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';
import { Nav } from '@/types/nav';

const navMap: Record<string, Nav> = {
  '/': 'today',
  '/storage': 'storage',
  '/recommend': 'recommend',
};

export const useActiveNav = (): Nav => {
  const location = useRouterState({
    select: (state) => state.location.pathname,
  });

  const previousNavRef = useRef<Nav>(null);

  useEffect(() => {
    if (navMap[location]) {
      previousNavRef.current = navMap[location];
    }
  }, [location]);

  return navMap[location] || previousNavRef.current;
};
