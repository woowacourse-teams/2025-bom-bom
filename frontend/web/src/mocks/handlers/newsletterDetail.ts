import { http, HttpResponse } from 'msw';
import { ENV } from '../../apis/env';
import { NEWSLETTER_DETAIL } from '../datas/newsletterDetail';

const baseURL = ENV.baseUrl;

export const newsletterDetailHandlers = [
  http.get(`${baseURL}/newsletters/:id`, ({ params }) => {
    const { id } = params;
    const newsletterDetail = NEWSLETTER_DETAIL.find(
      (newsletter) => newsletter.id === Number(id),
    );

    if (!newsletterDetail) {
      return new HttpResponse('Newsletter not found', { status: 404 });
    }

    return HttpResponse.json(newsletterDetail, { status: 200 });
  }),
];
