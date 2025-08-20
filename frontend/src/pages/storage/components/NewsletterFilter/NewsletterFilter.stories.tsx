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
    selectedNewsletterId: {
      control: 'number',
      description: '선택된 카테고리',
    },
    onSelectNewsletter: {
      action: 'selectedNewsletterChanged',
      description: '카테고리가 선택되었을 때 호출되는 함수',
    },
  },
  args: {
    newsLetterList: [
      { name: '기술', articleCount: 1, imageUrl: '' },
      { name: '경제', articleCount: 2, imageUrl: '' },
      { name: '디자인', articleCount: 3, imageUrl: '' },
      { name: '마케팅', articleCount: 4, imageUrl: '' },
      { name: '비즈니스', articleCount: 5, imageUrl: '' },
      { name: '문화', articleCount: 3, imageUrl: '' },
      { name: '건강', articleCount: 2, imageUrl: '' },
      { name: '여행', articleCount: 1, imageUrl: '' },
      { name: '푸드', articleCount: 3, imageUrl: '' },
      { name: '교육', articleCount: 0, imageUrl: '' },
    ],
    selectedNewsletterId: null,
    onSelectNewsletter: () => {},
  },
} satisfies Meta<typeof NewsletterFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: (args) => {
    const { selectedNewsletterId: initialNewsletterId, ...rest } = args;
    const [currentSelected, setCurrentSelected] = useState<number | null>(
      initialNewsletterId,
    );

    return (
      <NewsletterFilter
        {...rest}
        selectedNewsletterId={currentSelected}
        onSelectNewsletter={(id: number | null) => {
          setCurrentSelected(id);
        }}
      />
    );
  },
};
