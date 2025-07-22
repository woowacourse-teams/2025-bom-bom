import Tabs from './Tabs';
import Tab from '../Tab/Tab';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/common/Tabs',
  component: Tabs,
  args: {
    children: [
      <Tab
        key="봄봄1"
        onTabSelect={(id) => id}
        value="봄봄1"
        selected={false}
        label="봄봄1"
      />,
      <Tab
        key="봄봄2"
        onTabSelect={(id) => id}
        value="봄봄2"
        selected={true}
        label="봄봄2"
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
       <Tabs {...args} />
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
        <Tab
          onTabSelect={(id) => id}
          value="봄봄1"
          selected={false}
          label="봄봄1"
        />
        <Tab
          onTabSelect={(id) => id}
          value="봄봄2"
          selected={true}
          label="봄봄2"
        />
      </Tabs>
    );
  },
};
