import type { ArticleDetail } from '../../pages/detail/types/articleDetail';

export const PREVIOUS_ARTICLES = [
  {
    articleId: 123,
    title: '아티클 제목',
    contentsSummary: '아티클 요약 내용입니다...',
    expectedReadTime: 5,
  },
  {
    articleId: 124,
    title: '다른 아티클 제목',
    contentsSummary: '다른 아티클 요약입니다...',
    expectedReadTime: 3,
  },
];

export const PREVIOUS_ARTICLE_DETAILS: Record<
  number,
  Partial<ArticleDetail>
> = {
  123: {
    title: '아티클 제목',
    contents: '아티클의 전체 내용입니다...',
    arrivedDateTime: '2024-01-15T10:30:00',
    expectedReadTime: 5,
    newsletter: {
      name: '뉴스레터 이름',
      email: 'newsletter@example.com',
      imageUrl: 'https://example.com/image.jpg',
      category: '카테고리 이름',
    },
  },
  124: {
    title: '다른 아티클 제목',
    contents: '다른 아티클의 전체 내용입니다...',
    arrivedDateTime: '2024-01-20T08:00:00',
    expectedReadTime: 3,
    newsletter: {
      name: '뉴스레터 이름',
      email: 'newsletter@example.com',
      imageUrl: 'https://example.com/image.jpg',
      category: '카테고리 이름',
    },
  },
};
