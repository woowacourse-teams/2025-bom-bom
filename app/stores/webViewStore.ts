import { create } from 'zustand';
import { RNToWebMessage } from '../types/webview';

interface WebViewStore {
  webViewRef: React.RefObject<any> | null;
  setWebViewRef: (ref: React.RefObject<any>) => void;
  sendMessageToWeb: (message: RNToWebMessage) => void;
}

export const useWebViewStore = create<WebViewStore>((set, get) => ({
  webViewRef: null,

  setWebViewRef: (ref) => set({ webViewRef: ref }),

  sendMessageToWeb: (message) => {
    const { webViewRef } = get();
    try {
      const messageString = JSON.stringify(message);
      webViewRef?.current?.postMessage(messageString);
      console.log('WebView로 메시지 전송:', message);
    } catch (error) {
      console.error('WebView 메시지 전송 실패:', error);
    }
  },
}));
