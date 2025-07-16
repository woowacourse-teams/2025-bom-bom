import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Header from './Header';
import { Global, ThemeProvider } from '@emotion/react';
import { theme } from '../../styles/theme';
import reset from '../../styles/reset';

const meta: Meta<typeof Header> = {
  title: 'Components/Header',
  component: Header,
  parameters: {
    layout: 'fullscreen',
  },
};
export default meta;

type Story = StoryObj<typeof Header>;

export const Default: Story = {
  render: () => (
    <ThemeProvider theme={theme}>
      <Global styles={reset} />
      <Header />
    </ThemeProvider>
  ),
};
