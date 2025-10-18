export const isAndroidByUserAgent = () => {
  return /Android/i.test(navigator.userAgent);
};

export const isIOSByUserAgent = () => {
  return /iPhone|iPad|iPod/i.test(navigator.userAgent);
};

export const isMobileByUserAgent = () => {
  return isAndroidByUserAgent() || isIOSByUserAgent();
};
