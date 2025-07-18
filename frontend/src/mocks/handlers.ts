import { http, HttpResponse } from 'msw';
import { ARTICLES } from './data/mock-articles';

const baseURL = process.env.API_BASE_URL;

export const handlers = [
  http.get(`${baseURL}/articles`, () => {
    return HttpResponse.json({
      totalPages: 0,
      totalElements: 0,
      first: true,
      last: true,
      size: 0,
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
        pageSize: 0,
      },
      numberOfElements: 0,
      empty: true,
    });
  }),
];
