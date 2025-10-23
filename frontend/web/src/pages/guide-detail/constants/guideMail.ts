import type { GuideArticle } from '@/types/articles';

export const GUIDE_MAIL_STORAGE_KEY = 'guideMail';

export const GUIDE_MAILS: GuideArticle[] = [
  {
    articleId: 1,
    title: '[봄봄 가이드] 뉴스레터 구독하기',
    contentsSummary:
      '뉴스레터 추천에서 관심 카테고리 선택, 구독 페이지 접속, 봄봄 메일 붙여넣기까지 단계별 구독 방법을 안내합니다.',
    arrivedDateTime: '2025-08-18T11:00:00+09:00',
    expectedReadTime: 2,
    isRead: false,
    newsletter: {
      category: '가이드',
      name: '봄봄',
    },
  },
  {
    articleId: 2,
    title: '[봄봄 가이드] 키우기',
    contentsSummary:
      '봄이 캐릭터와 함께하는 읽기 습관 만들기. 출석과 아티클 읽기로 경험치를 모아 레벨 5까지 성장시키는 방법을 안내합니다.',
    arrivedDateTime: '2025-08-18T11:05:00+09:00',
    expectedReadTime: 2,
    isRead: false,
    newsletter: {
      category: '가이드',
      name: '봄봄',
    },
  },
  {
    articleId: 3,
    title: '[봄봄 가이드] 하이라이트 & 메모 사용법',
    contentsSummary:
      '텍스트 드래그로 하이라이트 만들기, 메모 추가하기, 좌측 바로가기에서 하이라이트 & 메모 모아보기까지 능동적 학습 방법을 안내합니다.',
    arrivedDateTime: '2025-08-18T11:10:00+09:00',
    expectedReadTime: 3,
    isRead: false,
    newsletter: {
      category: '가이드',
      name: '봄봄',
    },
  },
];
