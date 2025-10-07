import { gte } from 'semver';

const extractAppVersionFromUserAgent = (userAgent: string) => {
  const match = userAgent.match(/bombom\/(\d+\.\d+\.\d+)/);

  return match?.[1] ?? null;
};

export const isAppVersionSupported = (minVersion: string): boolean => {
  const version = extractAppVersionFromUserAgent(navigator.userAgent);

  if (!version) return false;

  return gte(version, minVersion);
};
