import styled from '@emotion/styled';
import { useLocation } from '@tanstack/react-router';
import { PropsWithChildren, useEffect } from 'react';
import Header from '../Header/Header';
import { useDevice } from '@/hooks/useDevice';
import {
  hideChannelButton,
  showChannelButton,
} from '@/libs/channelTalk/channelTalk.utils';
import { initChannelTalk } from '@/libs/channelTalk/initChannelTalk';

const PageLayout = ({ children }: PropsWithChildren) => {
  const device = useDevice();
  const location = useLocation();
  const isMobile = device === 'mobile';

  const isHeaderVisible =
    device === 'pc' || !location.pathname.startsWith('/articles/$articleId');

  useEffect(() => {
    initChannelTalk();
  }, []);

  useEffect(() => {
    if (device === 'pc') {
      showChannelButton();
    } else {
      hideChannelButton();
    }
  }, [device]);

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
