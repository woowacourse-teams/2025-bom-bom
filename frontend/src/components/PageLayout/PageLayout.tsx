import styled from '@emotion/styled';
import { PropsWithChildren } from 'react';
import Header from '../Header/Header';

function PageLayout({ children }: PropsWithChildren) {
  return (
    <Container>
      <Header />
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
