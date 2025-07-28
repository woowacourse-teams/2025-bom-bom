import { Global, ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { theme } from '@/styles/theme';
import reset from '@/styles/reset.ts';
import { createRouter, RouterProvider } from '@tanstack/react-router';
import { routeTree } from '@/routeTree.gen';

const router = createRouter({ routeTree });

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
        <RouterProvider router={router} />
        <Story />
      </ThemeProvider>
    ),
  ],
};

export default preview;
