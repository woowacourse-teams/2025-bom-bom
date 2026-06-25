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

export const Default: Story = {
  render: () => <Header activeNav="today" />,
};

export const Recommend: Story = {
  render: () => <Header activeNav="recommend" />,
};
