import { useRouterState } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';
import type { Nav } from '@/types/nav';

const navMap: Record<string, Nav> = {
  '/today': 'today',
  '/storage': 'storage',
  '/': 'recommend',
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
