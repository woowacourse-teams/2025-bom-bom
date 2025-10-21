export const isWebView = () => navigator.userAgent.includes('bombom');

export const getDeviceInWebView = () => {
  if (!isWebView()) return null;
  if (navigator.userAgent.includes('google')) return 'android';
  if (navigator.userAgent.includes('Apple')) return 'ios';
  return null;
};

export const getDeviceInWebApp = () => {
  if (isWebView()) return null;
  if (navigator.userAgent.includes('google')) return 'android';
  if (navigator.userAgent.includes('Apple')) return 'ios';
  return null;
};
