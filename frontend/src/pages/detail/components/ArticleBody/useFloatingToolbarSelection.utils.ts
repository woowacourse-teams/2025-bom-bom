import { isAndroid, isIOS, isWeb } from '@/libs/webview/webview.utils';

export const getFloatingToolbarEventMap = () => {
  if (isAndroid()) {
    return {
      selectionComplete: 'contextmenu',
      selectionClear: 'click',
    };
  }

  if (isIOS()) {
    return {
      selectionComplete: 'pointerup',
      selectionClear: 'selectionchange',
    };
  }

  if (isWeb()) {
    return {
      selectionComplete: 'mouseup',
      selectionClear: 'selectionchange',
    };
  }

  // fallback - web
  return {
    selectionComplete: 'mouseup',
    selectionClear: 'selectionchange',
  };
};
