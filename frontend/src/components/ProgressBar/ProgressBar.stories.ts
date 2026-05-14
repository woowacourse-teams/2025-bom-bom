import ProgressBar from './ProgressBar';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

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
    caption: '80%',
    rate: 80,
  },
};

export const Zero: Story = {
  args: {
    caption: '0%',
    rate: 0,
  },
};

export const Fulfill: Story = {
  args: {
    caption: '100%',
    rate: 100,
  },
};
