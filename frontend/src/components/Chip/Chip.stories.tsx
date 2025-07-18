import type { Meta, StoryObj } from '@storybook/react-webpack5';

import Chip from './Chip';

const meta = {
  title: 'components/common/Chip',
  component: Chip,
} satisfies Meta<typeof Chip>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    text: 'Chip',
    selected: false,
  },
};

export const Selected: Story = {
  args: {
    text: 'Chip',
    selected: true,
  },
};
