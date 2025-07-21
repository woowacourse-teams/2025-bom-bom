import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Select from './Select';
import { useState } from 'react';

const meta: Meta<typeof Select> = {
  title: 'components/common/Select',
  component: Select,
};

export default meta;
type Story = StoryObj<typeof Select>;

const fruitOptions = [
  { label: 'Apple', value: 'apple' },
  { label: 'Banana', value: 'banana' },
  { label: 'Grapes', value: 'grapes' },
  { label: 'Orange', value: 'orange' },
];

export const Default: Story = {
  render: () => {
    const [selected, setSelected] = useState<string | null>(null);

    return (
      <div style={{ padding: '40px' }}>
        <Select
          options={fruitOptions}
          selectedValue={selected}
          onSelectOption={(value) => setSelected(value)}
          placeholder="과일"
        />
      </div>
    );
  },
};

export const LongPlaceholder: Story = {
  render: () => {
    const [selected, setSelected] = useState<string | null>(null);

    return (
      <div style={{ padding: '40px' }}>
        <Select
          options={fruitOptions}
          selectedValue={selected}
          onSelectOption={(value) => setSelected(value)}
          placeholder="과일을 선택해주세요."
        />
      </div>
    );
  },
};

export const WideWidth: Story = {
  render: () => {
    const [selected, setSelected] = useState<string | null>(null);

    return (
      <div style={{ padding: '40px' }}>
        <Select
          options={fruitOptions}
          selectedValue={selected}
          onSelectOption={(value) => setSelected(value)}
          width={240}
          placeholder="과일을 선택해주세요."
        />
      </div>
    );
  },
};
