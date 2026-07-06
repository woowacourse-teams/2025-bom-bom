'use strict';

/**
 * Supabase PostgREST 얇은 헬퍼.
 * 환경변수 SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY 필요.
 */
async function sbFetch(path, { method = 'GET', body, headers = {} } = {}) {
  const baseUrl = process.env.SUPABASE_URL;
  const key = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!baseUrl || !key) {
    throw new Error('SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY 환경변수가 필요합니다');
  }

  const response = await fetch(`${baseUrl}/rest/v1/${path}`, {
    method,
    headers: {
      apikey: key,
      Authorization: `Bearer ${key}`,
      'Content-Type': 'application/json',
      Prefer: 'return=representation',
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Supabase ${method} ${path} 실패 (${response.status}): ${text}`);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

module.exports = { sbFetch };
