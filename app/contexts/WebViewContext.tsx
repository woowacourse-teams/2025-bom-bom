import React, {
  createContext,
  PropsWithChildren,
  useContext,
  useRef,
} from 'react';
import { WebView } from 'react-native-webview';

import { RNToWebMessage } from '../types/webview';

export interface WebViewContextType {
  webViewRef: React.RefObject<WebView | null>;
  sendMessageToWeb: (message: RNToWebMessage) => void;
}

const WebViewContext = createContext<WebViewContextType | undefined>(undefined);

export const WebViewProvider = ({ children }: PropsWithChildren) => {
  const webViewRef = useRef<WebView | null>(null);

  const sendMessageToWeb = (message: RNToWebMessage) => {
    try {
      const messageString = JSON.stringify(message);
      webViewRef.current?.postMessage(messageString);
      console.log('WebView로 메시지 전송:', message);
    } catch (error) {
      console.error('WebView 메시지 전송 실패:', error);
    }
  };

  return (
    <WebViewContext.Provider
      value={{
        webViewRef,
        sendMessageToWeb,
      }}
    >
      {children}
    </WebViewContext.Provider>
  );
};

export const useWebView = () => {
  const context = useContext(WebViewContext);
  if (context === undefined) {
    throw new Error('useWebView must be used within a WebViewProvider');
  }
  return context;
};
