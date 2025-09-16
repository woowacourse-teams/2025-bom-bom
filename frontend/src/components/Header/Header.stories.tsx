import Header from './Header';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta: Meta<typeof Header> = {
  title: 'components/common/Header',
  component: Header,
  parameters: {
    layout: 'fullscreen',
    docs: {
      story: {
        inline: false,
      },
    },
  },
};
export default meta;

type Story = StoryObj<typeof Header>;

export const PC: Story = {
  render: () => <Header variant="pc" />,
};

export const Mobile: Story = {
  render: () => <Header variant="mobile" />,
};

export const Tablet: Story = {
  render: () => <Header variant="tablet" />,
};

export const None: Story = {
  render: () => <Header variant="none" />,
};
