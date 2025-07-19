import type { Meta, StoryObj } from '@storybook/react-webpack5';
import NewsletterItemCard from './NewsletterItemCard';
import { Article } from '../../pages/today/types/article';

const meta: Meta<typeof NewsletterItemCard> = {
  title: 'components/bombom/NewsletterItemCard',
  component: NewsletterItemCard,
};

export default meta;
type Story = StoryObj<typeof meta>;

const mockArticle: Article = {
  articleId: '1',
  title: 'URL 변경만으로 100개 이상의 계정을 해킹한 방법 (IDOR + XSS 취약점)',
  contentsSummary:
    '$500 IDOR 취약점을 중요한 계정 탈취로 바꾼 버그 바운티 스토리입니다. 웹 보안의 기본이지만 놓치기 쉬운 취약점들에 대해 알아봅니다...',
  arrivedDateTime: new Date('2024-01-15T10:30:00Z'),
  thumbnailUrl:
    'https://images.unsplash.com/photo-1563206767-5b18f218e8de?w=525&h=224&fit=crop&crop=center',
  expectedReadTime: 8,
  isRead: false,
  newsletter: {
    name: 'UPPITY',
    imageUrl: 'https://example.com/uppity-logo.png',
    category: '기술',
  },
};

export const Default: Story = {
  args: {
    data: mockArticle,
  },
};

export const LongTitle: Story = {
  args: {
    data: {
      ...mockArticle,
      title:
        '매우 긴 제목의 예시입니다. 이런 경우 제목이 어떻게 표시되는지 확인해보겠습니다. 제목이 여러 줄로 나뉘어질 수 있습니다.',
      contentsSummary: '긴 제목을 가진 뉴스레터 아이템의 예시입니다.',
    },
  },
};

export const ShortContent: Story = {
  args: {
    data: {
      ...mockArticle,
      title: '짧은 제목',
      contentsSummary: '짧은 설명입니다.',
      expectedReadTime: 3,
      newsletter: {
        ...mockArticle.newsletter,
        name: 'TechNews',
        category: 'AI',
      },
    },
  },
};

export const DifferentCategories: Story = {
  args: {
    data: {
      ...mockArticle,
      title: '스타트업 투자 트렌드 2024',
      contentsSummary:
        '올해 스타트업 투자 시장의 주요 동향과 전망을 살펴봅니다. VC들이 주목하는 분야는?',
      thumbnailUrl:
        'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=525&h=224&fit=crop&crop=center',
      expectedReadTime: 12,
      newsletter: {
        ...mockArticle.newsletter,
        name: 'StartupToday',
        category: '비즈니스',
      },
    },
  },
};
