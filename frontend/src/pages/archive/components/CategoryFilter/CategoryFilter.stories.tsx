import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CategoryFilter from './CategoryFilter';
import { useState } from 'react';

const meta = {
  title: 'components/bombom/CategoryFilter',
  component: CategoryFilter,
  args: {
    categoryList: [
      { name: '기술', quantity: 1 },
      { name: '경제', quantity: 2 },
      { name: '디자인', quantity: 3 },
      { name: '마케팅', quantity: 4 },
      { name: '비즈니스', quantity: 5 },
      { name: '문화', quantity: 3 },
      { name: '건강', quantity: 2 },
      { name: '여행', quantity: 1 },
      { name: '푸드', quantity: 3 },
      { name: '교육', quantity: 0 },
    ],
    selectedCategory: '전체',
    onSelectCategory: () => {},
  },
} satisfies Meta<typeof CategoryFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: (args) => {
    const { selectedCategory: initialCategory, ...rest } = args;
    const [currentSelected, setCurrentSelected] =
      useState<string>(initialCategory);

    return (
      <CategoryFilter
        {...rest}
        selectedCategory={currentSelected}
        onSelectCategory={(id: string | number) => {
          setCurrentSelected(String(id));
        }}
      />
    );
  },
};
