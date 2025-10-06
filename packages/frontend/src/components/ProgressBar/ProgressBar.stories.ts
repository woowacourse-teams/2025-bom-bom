import ProgressBar from './ProgressBar';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/common/ProgressBar',
  component: ProgressBar,
  parameters: {
    layout: 'padded',
  },
  argTypes: {
    rate: {
      control: { type: 'range', min: 0, max: 100, step: 1 },
    },
    transition: {
      control: { type: 'select' },
      options: [false, true, 0, 0.2, 0.5, 1, 2],
    },
    variant: {
      control: { type: 'select' },
      options: ['rounded', 'rectangular'],
    },
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

export const Rectangular: Story = {
  args: {
    caption: '60%',
    rate: 60,
    variant: 'rectangular',
  },
};

export const NoAnimation: Story = {
  args: {
    caption: '40%',
    rate: 40,
    transition: false,
  },
};

export const SlowAnimation: Story = {
  args: {
    caption: '75%',
    rate: 75,
    transition: 2,
  },
};
