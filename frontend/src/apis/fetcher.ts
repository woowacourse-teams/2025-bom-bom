import ApiError from './ApiError';
import { ENV } from './env';

type FetcherOptions<TReq = unknown> = {
  path: string;
  query?: Record<string, string>;
  body?: TReq;
};

export const fetcher = {
  get: async <TRes>({ path, query }: FetcherOptions<never>) =>
    request<never, TRes>({ path, query, method: 'GET' }),
  post: async <TReq, TRes>({ path, body }: FetcherOptions<TReq>) =>
    request<TReq, TRes>({ path, body, method: 'POST' }),
  patch: async <TReq, TRes>({ path, body }: FetcherOptions<TReq>) =>
    request<TReq, TRes>({ path, body, method: 'PATCH' }),
  delete: async <TRes>({ path }: FetcherOptions<never>) =>
    request<never, TRes>({ path, method: 'DELETE' }),
};

type FetchMethod = 'GET' | 'POST' | 'PATCH' | 'DELETE';

type RequestOptions<TReq> = {
  path: string;
  method: FetchMethod;
  query?: Record<string, string>;
  body?: TReq;
  headers?: Record<string, string>;
};

const request = async <TReq, TRes>({
  path,
  method,
  query = {},
  body,
  headers,
}: RequestOptions<TReq>): Promise<TRes | null> => {
  const url = new URL(ENV.baseUrl + path);
  url.search = new URLSearchParams(query).toString();

  const config: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Basic ${ENV.token}`,
      ...headers,
    },
  };

  if (body && (method === 'POST' || method === 'PATCH')) {
    config.body = JSON.stringify(body);
  }

  const response = await fetch(url, config);

  if (!response.ok) {
    const status = response.status;
    const contentType = response.headers.get('Content-Type');
    let rawBody;
    let errorMessage = '알 수 없는 에러가 발생했습니다.';

    try {
      if (contentType?.includes('application/json')) {
        rawBody = await response.json();
        errorMessage = rawBody.message ?? errorMessage;
      } else {
        rawBody = await response.text();
        errorMessage = rawBody || errorMessage;
      }
    } catch {
      errorMessage = '응답 파싱에 실패했습니다.';
    }

    throw new ApiError(status, errorMessage, rawBody);
  }

  const contentType = response.headers.get('Content-Type');
  const status = response.status;

  if (status === 204) {
    return null;
  }

  if (contentType?.includes('application/json')) {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  return null;
};
