import { http, HttpResponse } from 'msw';
import { ENV } from '../../apis/env';
import { ARTICLES } from '../datas/articles';

const baseURL = ENV.baseUrl;
const BOOKMARKS: { id: number; articleId: number }[] = [];

export const bookmarkHandlers = [
  http.get(`${baseURL}/bookmarks`, () => {
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

  http.get(`${baseURL}/bookmarks/status/articles/:articleId`, ({ params }) => {
    const { articleId } = params;

    const bookmarked =
      BOOKMARKS.find((bookmark) => bookmark.articleId === Number(articleId)) ??
      false;
    return HttpResponse.json({ bookmarkStatus: bookmarked }, { status: 200 });
  }),

  http.post(`${baseURL}/bookmarks/articles/:articleId`, ({ params }) => {
    const { articleId } = params;
    const newBookmark = {
      id: BOOKMARKS.length + 1,
      articleId: Number(articleId),
    };

    BOOKMARKS.push(newBookmark);
    return new HttpResponse(null, { status: 204 });
  }),

  http.delete(`${baseURL}/bookmarks/articles/:articleId`, ({ params }) => {
    const { articleId } = params;
    const index = BOOKMARKS.findIndex(
      (bookmark) => bookmark.articleId === Number(articleId),
    );
    if (index === -1) {
      return new HttpResponse('Not Found', { status: 404 });
    }

    BOOKMARKS.splice(index, 1);
    return new HttpResponse(null, { status: 204 });
  }),
];
