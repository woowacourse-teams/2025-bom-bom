import { logger } from '../utils';
import ApiError from './ApiError';
import { ENV } from '@bombom/shared/env';

const DEFAULT_ERROR_MESSAGES: Record<number, string> = {
  400: '잘못된 요청입니다. 입력값을 확인해주세요.',
  401: '로그인이 필요합니다.',
  403: '접근 권한이 없습니다.',
  404: '요청한 리소스를 찾을 수 없습니다.',
  500: '서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.',
};

type JsonBody = Record<string, unknown> | unknown[];

type FetcherOptions<TRequest extends JsonBody> = {
  path: string;
  query?: Record<string, string | number | undefined | string[]>;
  body?: TRequest;
  headers?: HeadersInit;
};

export const fetcher = {
  get: async <TResponse>({ path, query }: FetcherOptions<never>) =>
    request<never, TResponse>({ path, query, method: 'GET' }),
  post: async <TRequest extends JsonBody, TResponse>({
    path,
    body,
    headers,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'POST', headers }),
  patch: async <TRequest extends JsonBody, TResponse>({
    path,
    query,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, query, body, method: 'PATCH' }),
  put: async <TRequest extends JsonBody, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'PUT' }),
  delete: async <TRequest extends JsonBody, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'DELETE' }),
};

type FetchMethod = 'GET' | 'POST' | 'PATCH' | 'DELETE' | 'PUT';

type RequestOptions<TRequest> = {
  path: string;
  method: FetchMethod;
  query?: Record<string, string | number | undefined | string[]>;
  body?: TRequest;
  headers?: HeadersInit;
};

const request = async <TRequest, TResponse>({
  path,
  method,
  query = {},
  body,
  headers,
}: RequestOptions<TRequest>): Promise<TResponse | undefined> => {
  try {
    const url = new URL(ENV.baseUrl + path);
    const stringifiedQuery: Record<string, string> = Object.fromEntries(
      Object.entries(query)
        .map(([key, value]) => [key, value?.toString()])
        .filter(([, value]) => value),
    );
    url.search = new URLSearchParams(stringifiedQuery).toString();

    const config: RequestInit = {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
    };

    if (body && (method === 'POST' || method === 'PATCH' || method === 'PUT')) {
      config.body = JSON.stringify(body);
    }

    const response = await fetch(url, config);

    if (!response.ok) {
      const status = response.status;
      const contentType = response.headers.get('Content-Type');
      let rawBody;
      let errorMessage = DEFAULT_ERROR_MESSAGES[status];

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

      throw new ApiError(status, errorMessage ?? '', rawBody);
    }

    const contentType = response.headers.get('Content-Type');

    if (contentType?.includes('application/json')) {
      try {
        return await response.json();
      } catch {
        return undefined;
      }
    }

    return undefined;
  } catch (error) {
    logger.error(error);
    throw error;
  }
};
