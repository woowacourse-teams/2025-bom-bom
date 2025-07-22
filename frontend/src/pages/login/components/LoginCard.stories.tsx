import type { Meta, StoryObj } from '@storybook/react-webpack5';
import LoginCard from './LoginCard';

const meta = {
  title: 'components/bombom/LoginCard',
  component: LoginCard,
} satisfies Meta<typeof LoginCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
