import type { Meta, StoryObj } from '@storybook/react-webpack5';
import React from 'react';
import SearchInput from './SearchInput';

const meta: Meta<typeof SearchInput> = {
  title: 'components/common/SearchInput',
  component: SearchInput,
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: '검색 기능을 위한 검색 입력 컴포넌트입니다.',
      },
    },
  },
  argTypes: {
    value: {
      control: 'text',
      description: '입력 필드의 현재 값',
    },
    onChange: {
      action: 'changed',
      description: '입력 값이 변경될 때 호출되는 콜백 함수',
    },
    placeholder: {
      control: 'text',
      description: '입력 필드의 플레이스홀더 텍스트',
    },
    disabled: {
      control: 'boolean',
      description: '입력 필드의 비활성화 여부',
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
    placeholder: '뉴스레터를 검색하세요...',
  },
};

export const Disabled: Story = {
  args: {
    value: '비활성화된 입력',
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
        story: '실시간 값 업데이트를 보여주는 인터랙티브 예시입니다.',
      },
    },
  },
};
