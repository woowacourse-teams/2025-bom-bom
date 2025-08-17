import { useState } from 'react';
import NewsletterFilter from './NewsletterFilter';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/bombom/NewsletterFilter',
  component: NewsletterFilter,
  parameters: {
    docs: {
      description: {
        component:
          '카테고리 필터 컴포넌트입니다. 카테고리를 선택할 수 있습니다.',
      },
    },
  },
  argTypes: {
    newsLetterList: {
      control: 'object',
      description: '카테고리 목록',
    },
    selectedNewsletter: {
      control: 'text',
      description: '선택된 카테고리',
    },
    onSelectNewsletter: {
      action: 'selectedNewsletterChanged',
      description: '카테고리가 선택되었을 때 호출되는 함수',
    },
  },
  args: {
    newsLetterList: [
      { newsletter: '기술', count: 1, imageUrl: '' },
      { newsletter: '경제', count: 2, imageUrl: '' },
      { newsletter: '디자인', count: 3, imageUrl: '' },
      { newsletter: '마케팅', count: 4, imageUrl: '' },
      { newsletter: '비즈니스', count: 5, imageUrl: '' },
      { newsletter: '문화', count: 3, imageUrl: '' },
      { newsletter: '건강', count: 2, imageUrl: '' },
      { newsletter: '여행', count: 1, imageUrl: '' },
      { newsletter: '푸드', count: 3, imageUrl: '' },
      { newsletter: '교육', count: 0, imageUrl: '' },
    ],
    selectedNewsletter: '전체',
    onSelectNewsletter: () => {},
  },
} satisfies Meta<typeof NewsletterFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: (args) => {
    const { selectedNewsletter: initialNewsletter, ...rest } = args;
    const [currentSelected, setCurrentSelected] =
      useState<string>(initialNewsletter);

    return (
      <NewsletterFilter
        {...rest}
        selectedNewsletter={currentSelected}
        onSelectNewsletter={(id: string | number) => {
          setCurrentSelected(String(id));
        }}
      />
    );
  },
};
