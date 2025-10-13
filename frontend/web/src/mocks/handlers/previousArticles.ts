import { http, HttpResponse } from 'msw';
import { ENV } from '../../apis/env';
import {
  PREVIOUS_ARTICLE_DETAILS,
  PREVIOUS_ARTICLES,
} from '../datas/previousArticles';

const baseURL = ENV.baseUrl;

export const previousArticleHandlers = [
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
];
