import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { useEffect } from 'react';
import { theme } from '../styles/theme';
import { ENV } from '@/apis/env';
import { usePageTracking } from '@/libs/googleAnalytics/usePageTracking';
import { queryClient } from '@/main';
import { initServiceWorkers } from '@/utils/serviceWorkerUtils';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();

  useEffect(() => {
    // 서비스 워커 충돌 방지 초기화
    initServiceWorkers(ENV.enableMsw === 'true', ENV.nodeEnv === 'production');
  }, []);

  return (
    <>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <Outlet />
        </ThemeProvider>
      </QueryClientProvider>
      <TanStackRouterDevtools />
    </>
  );
};

export const Route = createRootRouteWithContext<BomBomRouterContext>()({
  component: RootComponent,
});
