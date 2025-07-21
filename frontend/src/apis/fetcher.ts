import ApiError from './ApiError';
import { DEFAULT_ERROR_MESSAGES } from './constants/defaultErrorMessage';
import { ENV } from './env';

type FetcherOptions<TRequest extends Record<string, string>> = {
  path: string;
  query?: Record<string, string>;
  body?: TRequest;
};

export const fetcher = {
  get: async <TResponse>({ path, query }: FetcherOptions<never>) =>
    request<never, TResponse>({ path, query, method: 'GET' }),
  post: async <TRequest extends Record<string, string>, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'POST' }),
  patch: async <TRequest extends Record<string, string>, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'PATCH' }),
  put: async <TRequest extends Record<string, string>, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'PUT' }),
  delete: async <TResponse>({ path }: FetcherOptions<never>) =>
    request<never, TResponse>({ path, method: 'DELETE' }),
};

type FetchMethod = 'GET' | 'POST' | 'PATCH' | 'DELETE' | 'PUT';

type RequestOptions<TRequest> = {
  path: string;
  method: FetchMethod;
  query?: Record<string, string>;
  body?: TRequest;
  headers?: Record<string, string>;
};

const request = async <TRequest, TResponse>({
  path,
  method,
  query = {},
  body,
  headers,
}: RequestOptions<TRequest>): Promise<TResponse | null> => {
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

    throw new ApiError(status, errorMessage, rawBody);
  }

  const contentType = response.headers.get('Content-Type');

  if (contentType?.includes('application/json')) {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  return null;
};
