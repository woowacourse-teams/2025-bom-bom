import { Global, ThemeProvider } from '@emotion/react';
import type { Preview, Decorator } from '@storybook/react-webpack5';
import { theme } from '@/styles/theme';
import reset from '@/styles/reset.ts';
import {
  createRouter,
  createRootRoute,
  RouterProvider,
} from '@tanstack/react-router';

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
      <ThemeProvider theme={theme}>
        <Global styles={reset} />
        <Story />
      </ThemeProvider>
    ),
    RouterDecorator,
  ],
};

export default preview;
