import { ThemeProvider } from '@emotion/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createRootRouteWithContext,
  Outlet,
  redirect,
} from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';
import PageTitle from '@/components/PageTitle/PageTitle';
import Toast from '@/components/Toast/Toast';
import { queryClient } from '@/main';

interface BomBomRouterContext {
  queryClient: QueryClient;
}

const RootComponent = () => {
  return (
    <>
      <PageTitle />
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
