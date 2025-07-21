function requireEnv(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`환경 변수 ${name}가 설정되지 않았습니다.`);
  }
  return value;
}

export const ENV = {
  baseUrl: requireEnv('API_BASE_URL'),
  token: requireEnv('API_TOKEN'),
} as const;
