import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  Outlet,
  redirect,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import { queries } from '@/apis/queries';
import Toast from '@/components/Toast/Toast';
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
  beforeLoad: async ({ context, location }) => {
    const { queryClient } = context;

    try {
      const serverStatus = await queryClient.fetchQuery(queries.serverStatus());

      if (serverStatus !== 'UP') {
        if (location.pathname !== '/maintenance') {
          return redirect({ to: '/maintenance' });
        }
      }
    } catch {
      if (location.pathname !== '/maintenance') {
        return redirect({ to: '/maintenance' });
      }
    }
  },
});
