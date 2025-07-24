import NewsletterItemCard from './NewsletterItemCard';
import { ARTICLES } from '@/mocks/datas/articles';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta: Meta<typeof NewsletterItemCard> = {
  title: 'components/bombom/NewsletterItemCard',
  component: NewsletterItemCard,
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    data: ARTICLES[0],
  },
};

export const LongTitle: Story = {
  args: {
    data: {
      ...ARTICLES[0],
      title:
        '매우 긴 제목의 예시입니다. 이런 경우 제목이 어떻게 표시되는지 확인해보겠습니다. 제목이 여러 줄로 나뉘어질 수 있습니다.',
      contentsSummary: '긴 제목을 가진 뉴스레터 아이템의 예시입니다.',
    },
  },
};

export const ShortContent: Story = {
  args: {
    data: {
      ...ARTICLES[0],
      title: '짧은 제목',
      contentsSummary: '짧은 설명입니다.',
      expectedReadTime: 3,
      newsletter: {
        ...ARTICLES[0].newsletter,
        name: 'TechNews',
        category: 'AI',
      },
    },
  },
};

export const MultipleArticles: Story = {
  render: () => (
    <div style={{ display: 'flex', gap: '24px' }}>
      <NewsletterItemCard
        data={{
          ...ARTICLES[0],
          title: '첫 번째 뉴스레터',
          contentsSummary: '짧은 내용입니다.',
          newsletter: {
            ...ARTICLES[0].newsletter,
            name: 'Daily Tech',
            category: 'Technology',
          },
        }}
      />
      <NewsletterItemCard
        data={{
          ...ARTICLES[0],
          title: '두 번째 뉴스레터',
          contentsSummary:
            '긴 내용입니다만 아직 길지 않습니다. 현재 길어지는 중입니다. 더 길어질 예정입니다.',
          newsletter: {
            ...ARTICLES[0].newsletter,
            name: 'Finance Weekly',
            category: 'Finance',
          },
        }}
      />
      <NewsletterItemCard
        data={{
          ...ARTICLES[0],
          title:
            '긴 제목입니다만 아직 길지 않습니다. 현재 길어지는 중입니다. 더 길어질 예정입니다.',
          contentsSummary: '세 번째 뉴스레터입니다.',
          newsletter: {
            ...ARTICLES[0].newsletter,
            name: 'Startup News',
            category: 'Business',
          },
        }}
      />
    </div>
  ),
};
