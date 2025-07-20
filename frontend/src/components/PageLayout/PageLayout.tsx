import styled from '@emotion/styled';
import Header from '../Header/Header';
import { PropsWithChildren } from 'react';
import { NavType } from '../../types/nav';

interface PageLayoutProps {
  activeNav: NavType;
}

function PageLayout({
  activeNav,
  children,
}: PropsWithChildren<PageLayoutProps>) {
  return (
    <Container>
      <Header activeNav={activeNav} />
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
  padding-top: 64px; /* header 높이 */

  background-color: ${({ theme }) => theme.colors.white};
`;
