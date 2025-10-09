import { coerce, gte, valid } from 'semver';

const extractAppVersionFromUserAgent = (userAgent: string) => {
  const match = userAgent.match(/bombom\/(\d+\.\d+\.\d+)/);

  return match?.[1] ?? null;
};

const normalizeVersion = (version: string): string | null => {
  return valid(version) ?? coerce(version)?.version ?? null;
};

export const isAppVersionSupported = (minVersion: string): boolean => {
  const extractedVersion = extractAppVersionFromUserAgent(navigator.userAgent);
  if (!extractedVersion) return false;

  const normalizedVersion = normalizeVersion(extractedVersion);
  const normalizedMinVersion = normalizeVersion(minVersion);

  if (!normalizedVersion || !normalizedMinVersion) return false;

  return gte(normalizedVersion, normalizedMinVersion);
};
