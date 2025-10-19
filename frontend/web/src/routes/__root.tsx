import { theme } from '@bombom/shared/theme';
import { ThemeProvider } from '@emotion/react';
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import Toast from '@/components/Toast/Toast';
import usePageTracking from '@/libs/googleAnalytics/usePageTracking';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';
import type { QueryClient } from '@tanstack/react-query';
import type { redirect } from '@tanstack/react-router';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();
  useWebViewAuth();

  // SSR/CSR 모두 QueryClientProvider가 상위에서 제공됨
  // entry-server.tsx와 entry-client.tsx에서 각각 제공
  return (
    <>
      <ThemeProvider theme={theme}>
        <Outlet />
        <Toast />
      </ThemeProvider>
      <TanStackRouterDevtools />
    </>
  );
};

export const Route = createRootRouteWithContext<BomBomRouterContext>()({
  component: () => (
    <>
      <HeadContent />
      <RootComponent />
    </>
  ),
  beforeLoad: async (): Promise<void | ReturnType<typeof redirect>> => {
    return;
  },
});
