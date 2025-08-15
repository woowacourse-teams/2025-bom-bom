import { http, HttpResponse } from 'msw';
import { ENV } from '../apis/env';
import { ARTICLE_DETAIL } from './datas/articleDetail';
import { ARTICLES } from './datas/articles';
import { TRENDY_NEWSLETTERS } from './datas/trendyNewsLetter';
import { bookmarkHandlers } from './handlers/bookmark';
import { HighlightType } from '@/pages/detail/types/highlight';

const baseURL = ENV.baseUrl;

const HIGHLIGHTS: HighlightType[] = [];

export const handlers = [
  http.get(`${baseURL}/articles`, ({ request }) => {
    const url = new URL(request.url);
    const params = url.searchParams;

    const page = Number(params.get('page') ?? '0');
    const size = Number(params.get('size') ?? String(ARTICLES.length));
    const sortParam = params.get('sort') ?? 'arrivedDateTime,DESC';
    const [sortField, sortOrderRaw] = sortParam.split(',');
    const sortOrder = (sortOrderRaw ?? 'DESC').toUpperCase();
    const newsletter = params.get('newsletter') ?? undefined;
    const keyword = params.get('keyword') ?? undefined;

    let filtered = [...ARTICLES];

    if (newsletter) {
      filtered = filtered.filter(
        (a) => a.newsletter.name.toLowerCase() === newsletter.toLowerCase(),
      );
    }

    if (keyword) {
      const lower = keyword.toLowerCase();
      filtered = filtered.filter(
        (a) =>
          a.title.toLowerCase().includes(lower) ||
          a.newsletter.name.toLowerCase().includes(lower),
      );
    }

    const toTime = (dateStr: string) => {
      const parts = dateStr.split('.');
      if (parts.length >= 3) {
        const [y, m, d] = parts;
        return new Date(`${y}-${m}-${d}`).getTime();
      }
      return new Date(dateStr).getTime();
    };

    if (sortField === 'arrivedDateTime') {
      filtered.sort((a, b) => {
        const aTime = toTime(a.arrivedDateTime);
        const bTime = toTime(b.arrivedDateTime);
        return sortOrder === 'ASC' ? aTime - bTime : bTime - aTime;
      });
    }

    const totalElements = filtered.length;
    const totalPages = size > 0 ? Math.ceil(totalElements / size) : 1;
    const start = size > 0 ? page * size : 0;
    const end = size > 0 ? start + size : filtered.length;
    const content = size > 0 ? filtered.slice(start, end) : filtered;

    return HttpResponse.json({
      totalPages,
      totalElements,
      first: page === 0,
      last: page + 1 >= totalPages,
      size,
      content,
      number: page,
      sort: {
        empty: !sortParam,
        unsorted: !sortParam,
        sorted: !!sortParam,
      },
      pageable: {
        offset: start,
        sort: {
          empty: !sortParam,
          unsorted: !sortParam,
          sorted: !!sortParam,
        },
        unpaged: false,
        paged: true,
        pageNumber: page,
        pageSize: size,
      },
      numberOfElements: content.length,
      empty: content.length === 0,
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
          newsletter: 'UPPITY',
          count: 3,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: 'AI뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '스타트업뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '개발자뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '테크뉴스',
          count: 1,
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
          newsletter: 'UPPITY',
          count: 3,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: 'AI뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '스타트업뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '개발자뉴스',
          count: 1,
          imageUrl: 'https://newneek.co/favicon.ico',
        },
        {
          newsletter: '테크뉴스',
          count: 1,
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
  ...bookmarkHandlers,

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
];
