import React, {
  createContext,
  PropsWithChildren,
  useContext,
  useState,
} from 'react';

export interface AuthContextType {
  showWebViewLogin: boolean;
  setShowWebViewLogin: (show: boolean) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);
  const webViewRef = useRef<WebView>(null);

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
    <AuthContext.Provider
      value={{
        showWebViewLogin,
        setShowWebViewLogin,
        webViewRef,
        sendMessageToWeb,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
