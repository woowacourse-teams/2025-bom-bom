import { http, HttpResponse } from 'msw';
import { ENV } from '../apis/env';
import { ARTICLE_DETAIL } from './datas/articleDetail';
import { ARTICLES } from './datas/articles';
import { TRENDY_NEWSLETTERS } from './datas/trendyNewsLetter';
import { HighlightType } from '@/pages/detail/types/highlight';

const baseURL = ENV.baseUrl;

const HIGHLIGHTS: HighlightType[] = [];

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

  // 카테고리 통계
  http.get(`${baseURL}/articles/statistics/categories`, () => {
    return HttpResponse.json({
      totalCount: ARTICLES.length,
      categories: [{ category: '기술', count: ARTICLES.length }],
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
      id: 1,
      name: '봄봄이',
      level: 3,
      experience: 150,
      maxExperience: 200,
      imageUrl: '/assets/pet-1-lv3.png',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    });
  }),

  // 펫 출석 체크
  http.get(`${baseURL}/members/me/pet/attendance`, () => {
    return HttpResponse.json({
      id: 1,
      name: '봄봄이',
      level: 3,
      experience: 160,
      maxExperience: 200,
      imageUrl: '/assets/pet-1-lv3.png',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
      attendanceReward: 10,
    });
  }),

  // ------------------ 하이라이트 CRUD ------------------

  // 전체 조회
  http.get(`${baseURL}/highlight`, () => {
    return HttpResponse.json(HIGHLIGHTS);
  }),

  // 생성
  http.post(`${baseURL}/highlight`, async ({ request }) => {
    const newHighlight = (await request.json()) as HighlightType;
    // ID가 없는 경우 임의 ID 생성
    newHighlight.id = HIGHLIGHTS.length + 1;
    HIGHLIGHTS.push(newHighlight);
    return HttpResponse.json(newHighlight, { status: 201 });
  }),

  // 수정
  http.patch(`${baseURL}/highlight/:id`, async ({ request, params }) => {
    const { id } = params;
    const updated = (await request.json()) as Partial<
      Omit<HighlightType, 'id'>
    >;

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
  http.delete(`${baseURL}/highlight/:id`, ({ params }) => {
    const { id } = params;
    const index = HIGHLIGHTS.findIndex((h) => h.id === Number(id));
    if (index === -1) {
      return new HttpResponse('Not Found', { status: 404 });
    }
    HIGHLIGHTS.splice(index, 1);
    return new HttpResponse(null, { status: 204 });
  }),
];
