import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Header from './Header';

const meta: Meta<typeof Header> = {
  title: 'common/Header',
  component: Header,
  parameters: {
    layout: 'fullscreen',
  },
};
export default meta;

type Story = StoryObj<typeof Header>;

export const Default: Story = {
  render: () => <Header activeNav="home" />,
};

export const Recommend: Story = {
  render: () => <Header activeNav="recommend" />,
};
