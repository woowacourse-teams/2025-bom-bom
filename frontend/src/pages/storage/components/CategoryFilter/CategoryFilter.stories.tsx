import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CategoryFilter from './CategoryFilter';
import { useState } from 'react';

const meta = {
  title: 'components/bombom/CategoryFilter',
  component: CategoryFilter,
  parameters: {
    docs: {
      description: {
        component:
          '카테고리 필터 컴포넌트입니다. 카테고리를 선택할 수 있습니다.',
      },
    },
  },
  argTypes: {
    categoryList: {
      control: 'object',
      description: '카테고리 목록',
    },
    selectedValue: {
      control: 'text',
      description: '선택된 카테고리',
    },
    onSelectCategory: {
      action: 'selectedCategoryChanged',
      description: '카테고리가 선택되었을 때 호출되는 함수',
    },
  },
  args: {
    categoryList: [
      { value: '기술', label: '기술', quantity: 1 },
      { value: '경제', label: '경제', quantity: 2 },
      { value: '디자인', label: '디자인', quantity: 3 },
      { value: '마케팅', label: '마케팅', quantity: 4 },
      { value: '비즈니스', label: '비즈니스', quantity: 5 },
      { value: '문화', label: '문화', quantity: 3 },
      { value: '건강', label: '건강', quantity: 2 },
      { value: '여행', label: '여행', quantity: 1 },
      { value: '푸드', label: '푸드', quantity: 3 },
      { value: '교육', label: '교육', quantity: 0 },
    ],
    selectedValue: '전체',
    onSelectCategory: () => {},
  },
} satisfies Meta<typeof CategoryFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: (args) => {
    const { selectedValue: initialCategory, ...rest } = args;
    const [currentSelected, setCurrentSelected] =
      useState<string>(initialCategory);

    return (
      <CategoryFilter
        {...rest}
        selectedValue={currentSelected}
        onSelectCategory={(id: string | number) => {
          setCurrentSelected(String(id));
        }}
      />
    );
  },
};
