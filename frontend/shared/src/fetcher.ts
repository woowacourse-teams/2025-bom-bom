export type JsonBody = Record<string, unknown> | unknown[];

export type FetcherOptions<TRequest extends JsonBody> = {
  path: string;
  query?: Record<string, string | number | undefined | string[]>;
  body?: TRequest;
  headers?: HeadersInit;
};

export type FetchMethod = 'GET' | 'POST' | 'PATCH' | 'DELETE' | 'PUT';

export type RequestOptions<TRequest> = {
  path: string;
  method: FetchMethod;
  query?: Record<string, string | number | undefined | string[]>;
  body?: TRequest;
  headers?: HeadersInit;
};
