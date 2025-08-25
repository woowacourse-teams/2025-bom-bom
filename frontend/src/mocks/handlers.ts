import { http, HttpResponse } from 'msw';
import { ENV } from '../apis/env';
import { ARTICLE_DETAIL } from './datas/articleDetail';
import { ARTICLES } from './datas/articles';
import { TRENDY_NEWSLETTERS } from './datas/trendyNewsLetter';
import { bookmarkHandlers } from './handlers/bookmark';
import { newsletterDetailHandlers } from './handlers/newsletterDetail';
import { Highlight } from '@/pages/detail/types/highlight';

const baseURL = ENV.baseUrl;

const HIGHLIGHTS: Highlight[] = [];

export const handlers = [
  http.get(`${baseURL}/articles`, () => {
    return HttpResponse.json({
      totalPages: 1,
      totalElements: ARTICLES.length,
      first: true,
      last: true,
      size: ARTICLES.length,
      content: ARTICLES,
      number: 0,
      sort: {
        empty: true,
        unsorted: true,
        sorted: true,
      },
      pageable: {
        offset: 0,
        sort: {
          empty: true,
          unsorted: true,
          sorted: true,
        },
        unpaged: true,
        paged: true,
        pageNumber: 0,
        pageSize: ARTICLES.length,
      },
      numberOfElements: ARTICLES.length,
      empty: ARTICLES.length === 0,
    });
  }),

  // 기사 상세
  http.get(`${baseURL}/articles/:id`, () => {
    return HttpResponse.json(ARTICLE_DETAIL);
  }),

  // 기사 읽음 처리
  http.patch(`${baseURL}/articles/:id/read`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  // 뉴스레터 통계
  http.get(`${baseURL}/articles/statistics/newsletters`, () => {
    return HttpResponse.json({
      totalCount: ARTICLES.length,
      newsletters: [
        {
          newsletter: '테크뉴스',
          count: 5,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '개발자뉴스',
          count: 3,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: 'AI뉴스',
          count: 2,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
      ],
    });
  }),

  // 북마크 뉴스레터 통계
  http.get(`${baseURL}/bookmarks/statistics/newsletters`, () => {
    return HttpResponse.json({
      totalCount: 8,
      newsletters: [
        {
          newsletter: '테크뉴스',
          count: 3,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '개발자뉴스',
          count: 2,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: 'AI뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '스타트업뉴스',
          count: 2,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
      ],
    });
  }),

  // 뉴스레터 목록
  http.get(`${baseURL}/newsletters`, () => {
    return HttpResponse.json(TRENDY_NEWSLETTERS);
  }),

  // 멤버 읽기 상태
  http.get(`${baseURL}/members/me/reading`, () => {
    return HttpResponse.json({
      streakReadDay: 3,
      today: {
        readCount: 2,
        totalCount: 5,
      },
      weekly: {
        readCount: 7,
        goalCount: 10,
      },
    });
  }),

  // 펫 정보 조회
  http.get(`${baseURL}/members/me/pet`, () => {
    return HttpResponse.json({
      level: 1,
      currentStageScore: 100,
      requiredStageScore: 50,
    });
  }),

  // 펫 출석 체크
  http.post(`${baseURL}/members/me/pet/attendance`, () => {
    return HttpResponse.json();
  }),

  // ------------------ 하이라이트 CRUD ------------------

  // 전체 조회
  http.get(`${baseURL}/highlights`, () => {
    return HttpResponse.json({
      content: [...HIGHLIGHTS],
      pageable: {
        pageNumber: 0,
        pageSize: 10,
        sort: {
          empty: false,
          sorted: true,
          unsorted: false,
        },
        offset: 0,
        paged: true,
        unpaged: false,
      },
      totalElements: 2,
      totalPages: 1,
      last: true,
      size: 10,
      number: 0,
      sort: {
        empty: false,
        sorted: true,
        unsorted: false,
      },
      numberOfElements: 2,
      first: true,
      empty: false,
    });
  }),

  // 생성
  http.post(`${baseURL}/highlights`, async ({ request }) => {
    const newHighlight = (await request.json()) as Highlight;
    // ID가 없는 경우 임의 ID 생성
    newHighlight.id = HIGHLIGHTS.length + 1;
    HIGHLIGHTS.push(newHighlight);
    return HttpResponse.json(newHighlight, { status: 201 });
  }),

  // 수정
  http.patch(`${baseURL}/highlights/:id`, async ({ request, params }) => {
    const { id } = params;
    const updated = (await request.json()) as Partial<Omit<Highlight, 'id'>>;

    const index = HIGHLIGHTS.findIndex((h) => h.id === Number(id));
    if (index === -1 || !HIGHLIGHTS[index]) {
      return new HttpResponse('Not Found', { status: 404 });
    }

    HIGHLIGHTS[index] = {
      ...HIGHLIGHTS[index],
      ...updated,
    };
    return HttpResponse.json(HIGHLIGHTS[index]);
  }),

  // 삭제
  http.delete(`${baseURL}/highlights/:id`, ({ params }) => {
    const { id } = params;
    const index = HIGHLIGHTS.findIndex((h) => h.id === Number(id));
    if (index === -1) {
      return new HttpResponse('Not Found', { status: 404 });
    }
    HIGHLIGHTS.splice(index, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  // 뉴스레터별 하이라이트 통계
  http.get(`${baseURL}/highlights/statistics/newsletters`, () => {
    const newsletterStats = {
      totalCount: 4,
      newsletters: [
        {
          id: 1,
          newsletter: '뉴닉',
          imageUrl: 'https://newneek.co/favicon.ico',
          count: 1,
        },
        {
          id: 2,
          newsletter: '디에디트',
          imageUrl: 'https://newneek.co/favicon.ico',
          count: 3,
        },
      ],
    };
    return HttpResponse.json(newsletterStats);
  }),
  ...newsletterDetailHandlers,
  ...bookmarkHandlers,
];
