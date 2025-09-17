import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { useEffect } from 'react';
import { theme } from '../styles/theme';
import { ENV } from '@/apis/env';
import Toast from '@/components/Toast/Toast';
import ChannelService from '@/libs/channelTalk/ChannelService';
import { usePageTracking } from '@/libs/googleAnalytics/usePageTracking';
import { queryClient } from '@/main';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();

  useEffect(() => {
    ChannelService.loadScript();

    ChannelService.boot({
      pluginKey: ENV.pluginKey,
    });
  }, []);

  return (
    <>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <Outlet />
          <Toast />
        </ThemeProvider>
      </QueryClientProvider>
      <TanStackRouterDevtools />
    </>
  );
};

export const Route = createRootRouteWithContext<BomBomRouterContext>()({
  component: RootComponent,
});
