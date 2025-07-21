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
    text: '봄봄',
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
    id: 'tab',
    onSelect: (id) => id,
    text: '나침반',
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
    id: 'tab',
    onSelect: (id) => id,
    text: '나침반',
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
