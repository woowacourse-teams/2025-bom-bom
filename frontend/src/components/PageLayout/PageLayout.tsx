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

  const previousNavRef = useRef<NavType>(null);

  useEffect(() => {
    if (navMap[location]) {
      previousNavRef.current = navMap[location];
    }
  }, [location]);

  const activeNav = navMap[location] || previousNavRef.current;

  const isArticlePage = location.startsWith('/articles/');
  const isHeaderInvisible = isArticlePage && deviceType !== 'pc';

  return (
    <Container isMobile={isMobile} isHeaderInvisible={isHeaderInvisible}>
      <Header activeNav={activeNav} hideTopHeader={isHeaderInvisible} />
      {children}
    </Container>
  );
};

export default PageLayout;

const Container = styled.div<{
  isMobile: boolean;
  isHeaderInvisible: boolean;
}>`
  min-height: 100dvh;
  padding: ${({ isMobile, theme, isHeaderInvisible }) => {
    const sidePadding = isMobile ? '12px' : '24px';
    const headerHeight = isMobile
      ? theme.heights.headerMobile
      : theme.heights.headerPC;

    const topPadding = isHeaderInvisible
      ? `calc(env(safe-area-inset-top) + ${sidePadding})`
      : `calc(${headerHeight} + env(safe-area-inset-top) + ${sidePadding})`;
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
