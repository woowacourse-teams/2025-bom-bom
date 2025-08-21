import styled from '@emotion/styled';
import { useRouterState } from '@tanstack/react-router';
import { PropsWithChildren, useEffect, useRef } from 'react';
import Header from '../Header/Header';
import { useDeviceType } from '@/hooks/useDeviceType';
import { NavType } from '@/types/nav';

const navMap: Record<string, NavType> = {
  '/': 'today',
  '/storage': 'storage',
  '/recommend': 'recommend',
};

const PageLayout = ({ children }: PropsWithChildren) => {
  const location = useRouterState({
    select: (state) => state.location.pathname,
  });
  const deviceType = useDeviceType();
  const isMobile = deviceType === 'mobile';

  const previousNavRef = useRef<NavType>('today');

  useEffect(() => {
    if (navMap[location]) {
      previousNavRef.current = navMap[location];
    }
  }, [location]);

  const activeNav = navMap[location] || previousNavRef.current;

  return (
    <Container isMobile={isMobile}>
      <Header activeNav={activeNav} />
      {children}
    </Container>
  );
};

export default PageLayout;

const Container = styled.div<{ isMobile: boolean }>`
  min-height: 100dvh;
  padding: ${({ isMobile, theme }) => {
    const sidePadding = isMobile ? '12px' : '24px';
    const headerHeight = isMobile
      ? theme.heights.headerMobile
      : theme.heights.headerPC;

    const topPadding = `calc(${headerHeight} + env(safe-area-inset-top) + ${sidePadding})`;
    const bottomPadding = isMobile
      ? `calc(${theme.heights.bottomNav} + env(safe-area-inset-bottom) + ${sidePadding})`
      : `calc(env(safe-area-inset-bottom) + ${sidePadding})`;

    return `${topPadding} ${sidePadding} ${bottomPadding} ${sidePadding}`;
  }};

  display: flex;
  flex-direction: column;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};

  scrollbar-gutter: stable;
`;
