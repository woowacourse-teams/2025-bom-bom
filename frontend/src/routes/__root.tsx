import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import { queryClient } from '@/main';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

export const Route = createRootRouteWithContext<BomBomRouterContext>()({
  component: () => (
    <>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <Outlet />
        </ThemeProvider>
      </QueryClientProvider>
      <TanStackRouterDevtools />
    </>
  ),
});
