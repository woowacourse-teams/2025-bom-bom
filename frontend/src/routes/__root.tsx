import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import Toast from '@/components/Toast/Toast';
import { usePageTracking } from '@/libs/googleAnalytics/usePageTracking';
import { queryClient } from '@/main';
import type { QueryClient } from '@tanstack/react-query';
import type { redirect } from '@tanstack/react-router';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  usePageTracking();
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
  beforeLoad: async (): Promise<void | ReturnType<typeof redirect>> => {
    return;
  },
});
