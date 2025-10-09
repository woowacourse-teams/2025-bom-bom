import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '@bombom/shared/theme';
import Toast from '@/components/Toast/Toast';
import usePageTracking from '@/libs/googleAnalytics/usePageTracking';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';
import { queryClient } from '@/main';
import type { QueryClient } from '@tanstack/react-query';
import type { redirect } from '@tanstack/react-router';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();
  useWebViewAuth();

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
