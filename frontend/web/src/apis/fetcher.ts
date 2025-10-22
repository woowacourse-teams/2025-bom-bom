import ApiError from './ApiError';
import { DEFAULT_ERROR_MESSAGES } from './constants/defaultErrorMessage';
import { ENV } from './env';
import { logger } from '@/utils/logger';
import type {
  FetcherOptions,
  JsonBody,
  RequestOptions,
} from '@bombom/shared/fetcher';

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
        Authorization: `Basic ${ENV.token}`,
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
