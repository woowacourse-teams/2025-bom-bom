import { ENV } from '@/constants/env';
import type {
  FetcherOptions,
  JsonBody,
  RequestOptions,
} from '@bombom/shared/fetcher';
import { ApiError, DEFAULT_ERROR_MESSAGES } from '@bombom/shared';

export const fetcher = {
  post: async <TRequest extends JsonBody, TResponse>({
    path,
    body,
    headers,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'POST', headers }),
  put: async <TRequest extends JsonBody, TResponse>({
    path,
    body,
  }: FetcherOptions<TRequest>) =>
    request<TRequest, TResponse>({ path, body, method: 'PUT' }),
};

const request = async <TRequest, TResponse>({
  path,
  method,
  body,
  headers,
}: RequestOptions<TRequest>): Promise<TResponse | undefined> => {
  try {
    const config: RequestInit = {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
    };

    if (body) {
      config.body = JSON.stringify(body);
    }

    const response = await fetch(`${ENV.baseUrl}${path}`, config);

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
    console.error(error);
    throw error;
  }
};
