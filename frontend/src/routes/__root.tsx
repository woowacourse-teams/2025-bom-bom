import { ThemeProvider } from '@emotion/react';
import { createRootRoute, Outlet } from '@tanstack/react-router';
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools';
import { theme } from '../styles/theme';

export const Route = createRootRoute({
  component: () => (
    <>
      <ThemeProvider theme={theme}>
        <Outlet />
      </ThemeProvider>
      <TanStackRouterDevtools />
    </>
  ),
});
