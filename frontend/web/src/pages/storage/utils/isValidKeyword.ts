export const isValidKeyword = (value?: string): boolean => {
  if (typeof value !== 'string') return false;
  const trimmed = value.trim();
  return trimmed.length >= 2 && trimmed.length <= 100;
};
