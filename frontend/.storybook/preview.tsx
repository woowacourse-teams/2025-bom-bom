import { Global, ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import reset from '../src/styles/reset';
import { theme } from '../src/styles/theme';

const preview: Preview = {
  parameters: {
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
