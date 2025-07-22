import Tab from './Tab';
import CompassIcon from '../icons/CompassIcon';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

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
    value: 'bombom',
    label: '봄봄',
    onTabSelect: (id) => id,
  },
  render: (args) => {
    return (
      <>
        <Tab {...args} selected={false} />
        <Tab {...args} selected={true} />
      </>
    );
  },
};

export const WithEndIcon: Story = {
  args: {
    value: 'compass',
    label: '나침반',
    onTabSelect: (id) => id,
    StartComponent: <CompassIcon />,
  },
  render: (args) => {
    return (
      <>
        <Tab {...args} selected={false} />
        <Tab {...args} selected={true} />
      </>
    );
  },
};

export const WithStartIcon: Story = {
  args: {
    value: 'compass',
    label: '나침반',
    onTabSelect: (id) => id,
    StartComponent: <CompassIcon />,
  },
  render: (args) => {
    return (
      <>
        <Tab {...args} selected={false} />
        <Tab {...args} selected={true} />
      </>
    );
  },
};
