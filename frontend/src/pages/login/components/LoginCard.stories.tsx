import LoginCard from './LoginCard';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/bombom/LoginCard',
  component: LoginCard,
} satisfies Meta<typeof LoginCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const PC: Story = {
  args: {
    isMobile: false,
  },
};

export const Tablet: Story = {
  args: {
    isMobile: false,
  },
};

export const Mobile: Story = {
  args: {
    isMobile: true,
  },
};
