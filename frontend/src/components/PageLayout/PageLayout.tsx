import styled from '@emotion/styled';
import { useRouterState } from '@tanstack/react-router';
import { PropsWithChildren } from 'react';
import Header from '../Header/Header';
import { NavType } from '@/types/nav';

const navMap: Record<string, NavType> = {
  '/': 'today',
  '/storage': 'storage',
  '/recommend': 'recommend',
};

function PageLayout({ children }: PropsWithChildren) {
  const location = useRouterState({
    select: (state) => state.location.pathname,
  });

  return (
    <Container>
      <Header activeNav={navMap[location] || 'today'} />
      {children}
    </Container>
  );
}

export default PageLayout;

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;

  width: 100%;
  padding: 72px 0; /* header 높이 */

  background-color: ${({ theme }) => theme.colors.white};
`;
