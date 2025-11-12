import { theme } from '@bombom/shared/theme';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
  redirect,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { ENV } from '@/apis/env';
import Toast from '@/components/Toast/Toast';
import { SERVER_STATUS } from '@/constants/serverStatus';
import usePageTracking from '@/libs/googleAnalytics/usePageTracking';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';
import { queryClient } from '@/main';
import type { QueryClient } from '@tanstack/react-query';

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
  beforeLoad: async ({
    location,
  }): Promise<void | ReturnType<typeof redirect>> => {
    const maintenancePath = '/maintenance';

    try {
      const res = await fetch(ENV.monitoringStatusUrl, {
        cache: 'no-store',
      });

      if (!res.ok) {
        throw new Error(`Failed to fetch status: ${res.status}`);
      }

      const serverStatus = await res.json();
      const status =
        serverStatus?.data?.result?.[0]?.value[1] ?? SERVER_STATUS.off;

      const isServerOn = status === SERVER_STATUS.on;

      if (!isServerOn && location.pathname !== maintenancePath) {
        return redirect({ to: maintenancePath });
      }
    } catch (err) {
      console.error('Server status check failed:', err);
      if (location.pathname !== maintenancePath) {
        return redirect({ to: maintenancePath });
      }
    }

    return;
  },
});
