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

  http.get(`${baseURL}/bookmarks/status/:articleId`, async ({ request }) => {
    const { articleId } = (await request.json()) as { articleId: number };

    const bookmarked =
      BOOKMARKS.find((bookmark) => bookmark.articleId === articleId) ?? false;
    return HttpResponse.json({ data: bookmarked }, { status: 200 });
  }),

  http.post(`${baseURL}/bookmarks/:articleId`, async ({ request }) => {
    const { articleId } = (await request.json()) as { articleId: number };
    const newBookmark = {
      id: BOOKMARKS.length + 1,
      articleId,
    };

    BOOKMARKS.push(newBookmark);
    return HttpResponse.json({ data: newBookmark }, { status: 201 });
  }),

  http.delete(`${baseURL}/bookmarks/:articleId`, ({ params }) => {
    const { id } = params;
    const index = BOOKMARKS.findIndex(
      (bookmark) => bookmark.articleId === Number(id),
    );
    if (index === -1) {
      return new HttpResponse('Not Found', { status: 404 });
    }

    BOOKMARKS.splice(index, 1);
    return new HttpResponse(null, { status: 204 });
  }),
];
