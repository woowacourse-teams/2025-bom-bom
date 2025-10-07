import { Global, ThemeProvider } from '@emotion/react';
import type { Preview, Decorator } from '@storybook/react-webpack5';
import { theme } from 'shared/theme';
import reset from '@/styles/reset.ts';
import {
  createRouter,
  createRootRoute,
  RouterProvider,
} from '@tanstack/react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

const RouterDecorator: Decorator = (Story) => {
  const rootRoute = createRootRoute({
    component: () => <Story />,
  });

  const router = createRouter({
    routeTree: rootRoute,
  });

  return <RouterProvider router={router} />;
};

const preview: Preview = {
  parameters: {
    layout: 'centered',
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <Global styles={reset} />
          <Story />
        </ThemeProvider>
      </QueryClientProvider>
    ),
    RouterDecorator,
  ],
};

export default preview;
