import type { Meta, StoryObj } from '@storybook/react-webpack5';
import React from 'react';
import SearchInput from './SearchInput';

const meta: Meta<typeof SearchInput> = {
  title: 'Components/SearchInput',
  component: SearchInput,
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component:
          'A search input component with Korean placeholder text for newsletter search functionality.',
      },
    },
  },
  argTypes: {
    value: {
      control: 'text',
      description: 'The current value of the input',
    },
    onChange: {
      action: 'changed',
      description: 'Callback function when input value changes',
    },
    placeholder: {
      control: 'text',
      description: 'Placeholder text for the input',
    },
    disabled: {
      control: 'boolean',
      description: 'Whether the input is disabled',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};

export const WithValue: Story = {
  args: {
    value: '뉴스레터',
  },
};

export const CustomPlaceholder: Story = {
  args: {
    placeholder: 'Search newsletters...',
  },
};

export const Disabled: Story = {
  args: {
    value: 'Disabled input',
    disabled: true,
  },
};

export const Interactive: Story = {
  render: () => {
    const [value, setValue] = React.useState('');

    return (
      <SearchInput
        value={value}
        onChange={setValue}
        placeholder="뉴스레터 제목이나 발행처로 검색하세요..."
      />
    );
  },
  parameters: {
    docs: {
      description: {
        story: 'Interactive example showing real-time value updates.',
      },
    },
  },
};
