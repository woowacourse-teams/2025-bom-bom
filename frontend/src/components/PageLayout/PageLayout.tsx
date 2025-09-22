import styled from '@emotion/styled';
import { useLocation } from '@tanstack/react-router';
import { PropsWithChildren, useEffect } from 'react';
import Header from '../Header/Header';
import { useDeviceType } from '@/hooks/useDeviceType';
import { useChannelTalk } from '@/libs/channelTalk/useChannelTalk';

const PageLayout = ({ children }: PropsWithChildren) => {
  const deviceType = useDeviceType();
  const location = useLocation();
  const { showChannelButton, hideChannelButton } = useChannelTalk();
  const isMobile = deviceType === 'mobile';

  const isHeaderVisible =
    deviceType === 'pc' ||
    !location.pathname.startsWith('/articles/$articleId');

  useEffect(() => {
    if (deviceType === 'pc') {
      showChannelButton();
    } else {
      hideChannelButton();
    }
  }, [hideChannelButton, showChannelButton, deviceType]);

  return (
    <Container isMobile={isMobile}>
      {isHeaderVisible && <Header />}
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
