import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ProgressBar from './ProgressBar';

const meta = {
  title: 'components/common/ProgressBar',
  component: ProgressBar,
  parameters: {
    layout: 'padded',
  },
} satisfies Meta<typeof ProgressBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    rate: 80,
  },
};

export const Zero: Story = {
  args: {
    rate: 0,
  },
};

export const Fulfill: Story = {
  args: {
    rate: 100,
  },
};
