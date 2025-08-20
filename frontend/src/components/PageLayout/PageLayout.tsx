import styled from '@emotion/styled';
import { useRouterState } from '@tanstack/react-router';
import { PropsWithChildren, useEffect, useRef } from 'react';
import Header from '../Header/Header';
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

  const previousNavRef = useRef<NavType>('today');

  useEffect(() => {
    if (navMap[location]) {
      previousNavRef.current = navMap[location];
    }
  }, [location]);

  const activeNav = navMap[location] || previousNavRef.current;

  return (
    <Container>
      <Header activeNav={activeNav} />
      {children}
    </Container>
  );
};

export default PageLayout;

const Container = styled.div`
  width: 100%;
  min-height: 100vh;
  padding: 72px 0; /* header 높이 */

  display: flex;
  flex-direction: column;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};
`;
