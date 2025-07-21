import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Tabs from './Tabs';
import Tab from '../Tab/Tab';

const meta = {
  title: 'components/common/Tabs',
  component: Tabs,
  args: {
    children: [
      <Tab
        key="봄봄1"
        id="tab"
        onSelect={(id) => id}
        text="봄봄"
        selected={false}
      />,
      <Tab
        key="봄봄2"
        id="tab"
        onSelect={(id) => id}
        text="봄봄"
        selected={true}
      />,
    ],
  },
} satisfies Meta<typeof Tabs>;

export default meta;
type Story = StoryObj<typeof meta>;

export const HorizontalTabs: Story = {
  args: {
    direction: 'horizontal',
  },
  render: (args) => {
    return (
      <Tabs {...args}>
        <Tab id="tab" onSelect={(id) => id} text="봄봄" selected={false} />
        <Tab id="tab" onSelect={(id) => id} text="봄봄" selected={true} />
      </Tabs>
    );
  },
};

export const VerticalTabs: Story = {
  args: {
    direction: 'vertical',
  },
  render: (args) => {
    return (
      <Tabs {...args}>
        <Tab id="tab" onSelect={(id) => id} text="봄봄" selected={false} />
        <Tab id="tab" onSelect={(id) => id} text="봄봄" selected={true} />
      </Tabs>
    );
  },
};
