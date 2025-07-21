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
    name: '봄봄',
    onSelect: (id) => id,
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

export const WithLeadingIcon: Story = {
  args: {
    name: '나침반',
    onSelect: (id) => id,
    LeadingComponent: <CompassIcon />,
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

export const WithTrailingIcon: Story = {
  args: {
    name: '나침반',
    onSelect: (id) => id,
    TrailingComponent: <CompassIcon />,
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
