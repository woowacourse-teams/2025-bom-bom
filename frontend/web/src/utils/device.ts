export const isWebView = () => navigator.userAgent.includes('bombom');

export const getDeviceInWebView = () => {
  if (!isWebView()) return null;
  if (navigator.userAgent.includes('google')) return 'android';
  if (navigator.userAgent.includes('Apple')) return 'ios';
  return null;
};

export const getDeviceInWebApp = () => {
  if (isWebView()) return null;
  if (/Android/i.test(navigator.userAgent)) return 'android';
  if (/iPhone|iPad|iPod/i.test(navigator.userAgent)) return 'ios';
  return null;
};

export const getDevice = () => {
  if (isWebView()) {
    return getDeviceInWebView();
  } else {
    return getDeviceInWebApp();
  }
};

export const isAndroid = () => {
  return getDevice() === 'android';
};

export const isIOS = () => {
  return getDevice() === 'ios';
};
