import styled from '@emotion/styled';
import { useLocation } from '@tanstack/react-router';
import { PropsWithChildren } from 'react';
import Header from '../Header/Header';
import { useDevice } from '@/hooks/useDevice';

const PageLayout = ({ children }: PropsWithChildren) => {
  const device = useDevice();
  const location = useLocation();
  const isMobile = device === 'mobile';

  const isHeaderInvisible =
    device !== 'pc' && location.pathname.startsWith('/articles/$articleId');
  const headerVariant = isHeaderInvisible ? 'none' : device;

  return (
    <Container isMobile={isMobile}>
      <Header variant={headerVariant} />
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
