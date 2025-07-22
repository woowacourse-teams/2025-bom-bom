import { Global, ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { theme } from '@/styles/theme';
import reset from '@/styles/reset.ts';

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
  ],
};

export default preview;
