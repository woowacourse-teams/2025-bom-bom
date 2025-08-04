export const ENV = {
  baseUrl: `${process.env.API_BASE_URL}`,
  token: `${process.env.API_TOKEN}`,
  enableMsw: `${process.env.ENABLE_MSW}`,
  sentryDsn: `${process.env.SENTRY_DSN}`,
} as const;
