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
      { id: 1, name: '기술', articleCount: 1, imageUrl: '' },
      { id: 2, name: '경제', articleCount: 2, imageUrl: '' },
      { id: 3, name: '디자인', articleCount: 3, imageUrl: '' },
      { id: 4, name: '마케팅', articleCount: 4, imageUrl: '' },
      { id: 5, name: '비즈니스', articleCount: 5, imageUrl: '' },
      { id: 6, name: '문화', articleCount: 3, imageUrl: '' },
      { id: 7, name: '건강', articleCount: 2, imageUrl: '' },
      { id: 8, name: '여행', articleCount: 1, imageUrl: '' },
      { id: 9, name: '푸드', articleCount: 3, imageUrl: '' },
      { id: 10, name: '교육', articleCount: 0, imageUrl: '' },
    ],
    selectedNewsletterId: null,
    onSelectNewsletter: () => {},
  },
} satisfies Meta<typeof NewsletterFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => {
    return <NewsletterFilter />;
  },
};
