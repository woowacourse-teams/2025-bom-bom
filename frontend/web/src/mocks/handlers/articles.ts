import { http, HttpResponse } from 'msw';
import { ARTICLE_DETAIL } from '../datas/articleDetail';
import { ARTICLES } from '../datas/articles';
import {
  PREVIOUS_ARTICLE_DETAILS,
  PREVIOUS_ARTICLES,
} from '../datas/previousArticles';
import { ENV } from '@/apis/env';

const baseURL = ENV.baseUrl;

export const articleHandlers = [
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

  // 지난 뉴스레터 목록
  http.get(`${baseURL}/articles/previous`, ({ request }) => {
    const url = new URL(request.url);
    const newsletterId = url.searchParams.get('newsletterId');
    const limit = url.searchParams.get('limit');

    if (!newsletterId || Number(newsletterId) <= 0) {
      return HttpResponse.json(
        { message: 'newsletterId는 1 이상의 값이어야 합니다.' },
        { status: 400 },
      );
    }

    if (!limit || Number(limit) <= 0 || Number(limit) > 10) {
      return HttpResponse.json(
        { message: 'limit은 1 이상 10 이하의 값이어야 합니다.' },
        { status: 400 },
      );
    }

    return HttpResponse.json(PREVIOUS_ARTICLES.slice(0, Number(limit)), {
      status: 200,
    });
  }),

  // 지난 뉴스레터 상세
  http.get(`${baseURL}/articles/previous/:id`, ({ params }) => {
    const { id } = params;
    const article = PREVIOUS_ARTICLE_DETAILS[Number(id)];

    if (!article) {
      return HttpResponse.json(
        { message: '해당 아티클을 찾을 수 없습니다.' },
        { status: 404 },
      );
    }

    return HttpResponse.json(article, { status: 200 });
  }),

  // 아티클 상세
  http.get(`${baseURL}/articles/:id`, () => {
    return HttpResponse.json(ARTICLE_DETAIL);
  }),

  // 아티클 읽음 처리
  http.patch(`${baseURL}/articles/:id/read`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  // 아티클 목록
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
];
