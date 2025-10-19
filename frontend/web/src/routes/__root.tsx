import { theme } from '@bombom/shared/theme';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider, type QueryClient } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import Toast from '@/components/Toast/Toast';
import usePageTracking from '@/libs/googleAnalytics/usePageTracking';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';
import { queryClient } from '@/main';
import type { redirect } from '@tanstack/react-router';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();
  useWebViewAuth();

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <Outlet />
        <Toast />
      </ThemeProvider>
      <TanStackRouterDevtools />
    </QueryClientProvider>
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
