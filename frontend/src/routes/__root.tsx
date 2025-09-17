import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  Outlet,
  redirect,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import { ENV } from '@/apis/env';
import Toast from '@/components/Toast/Toast';
import { SERVER_STATUS } from '@/constants/serverStatus';
import { usePageTracking } from '@/libs/googleAnalytics/usePageTracking';
import { queryClient } from '@/main';

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
  beforeLoad: async ({ location }) => {
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

      const isUp = status === SERVER_STATUS.on;

      if (!isUp && location.pathname !== maintenancePath) {
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
