import { Article } from '../pages/today/types/article';

export const MOCK_ARTICLES: Article = {
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
