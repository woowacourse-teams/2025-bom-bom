import type { Meta, StoryObj } from '@storybook/react-webpack5';

import ProgressBar from '../components/ProgressBar';

const meta = {
  title: 'ProgressBar',
  component: ProgressBar,
} satisfies Meta<typeof ProgressBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    progressRate: 80,
  },
};

export const Zero: Story = {
  args: {
    progressRate: 0,
  },
};

export const Fulfill: Story = {
  args: {
    progressRate: 100,
  },
};
