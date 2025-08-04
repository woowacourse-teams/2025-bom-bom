import { http, HttpResponse } from 'msw';
import { ENV } from '../apis/env';
import { ARTICLE_DETAIL } from './datas/articleDetail';
import { ARTICLES } from './datas/articles';
import { TRENDY_NEWSLETTERS } from './datas/trendyNewsLetter';

const baseURL = ENV.baseUrl;

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
  http.get(new RegExp(`${baseURL}/articles/\\d+$`), () => {
    return HttpResponse.json(ARTICLE_DETAIL);
  }),

  // 기사 읽음 처리
  http.patch(new RegExp(`${baseURL}/articles/\\d+/read`), () => {
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
];
