import RequireLoginCard from './RequireLoginCard';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/common/RequireLoginCard',
  component: RequireLoginCard,
} satisfies Meta<typeof RequireLoginCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
