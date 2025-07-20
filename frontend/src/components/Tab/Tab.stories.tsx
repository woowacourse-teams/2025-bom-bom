import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Tab from './Tab';
import CompassIcon from '../icons/CompassIcon';

const meta = {
  title: 'components/common/Tab',
  component: Tab,
  decorators: [
    (Story) => (
      <div style={{ display: 'flex' }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof Tab>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    id: 'tab',
    onSelect: (id) => id,
  },
  render: (args) => {
    return (
      <>
        <Tab {...args} selected={false}>
          Tab1
        </Tab>
        <Tab {...args} selected={true}>
          Tab2
        </Tab>
      </>
    );
  },
};

export const WithMultipleNode: Story = {
  args: {
    id: 'tab',
    onSelect: (id) => id,
    children: (
      <div
        style={{
          width: '100px',
          display: 'flex',
          justifyContent: 'space-between',
        }}
      >
        <p>나침반</p>
        <CompassIcon />
      </div>
    ),
  },
  render: (args) => {
    const { children, ...props } = args;
    return (
      <>
        <Tab {...props} selected={false}>
          {children}
        </Tab>
        <Tab {...props} selected={true}>
          {children}
        </Tab>
      </>
    );
  },
};
