import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createRootRoute, Outlet, redirect } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import { getUserInfo } from '@/apis/members';

const queryClient = new QueryClient();

let isFirstCheck = true;

export const Route = createRootRoute({
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
  beforeLoad: async ({ location }) => {
    if (!isFirstCheck) {
      return;
    }

    try {
      await queryClient.fetchQuery({
        queryKey: ['me'],
        queryFn: getUserInfo,
        retry: false,
      });
    } catch {
      if (location.pathname !== '/recommend') {
        throw redirect({ to: '/recommend' });
      }
    } finally {
      isFirstCheck = false;
    }
  },
});
