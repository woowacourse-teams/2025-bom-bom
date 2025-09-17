import { useEffect } from 'react';
import { postGoogleLogin, postAppleLogin } from '@/apis/auth';
import {
  addWebViewMessageListener,
  RNToWebMessage,
  sendMessageToRN,
} from '@/utils/webviewBridge';

/**
 * WebView와 React Native 간의 메시지를 수신하고 처리하는 컴포넌트
 * 앱의 최상위 레벨에서 사용되어야 합니다.
 */
export const WebViewMessenger: React.FC = () => {
  useEffect(() => {
    // React Native에서 온 메시지를 수신하는 리스너 등록
    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        switch (message.type) {
          case 'GOOGLE_LOGIN_TOKEN':
            console.log('Google 로그인 토큰 수신:', message.payload);
            if (!message.payload?.idToken) return;
            try {
              alert(JSON.stringify(message.payload));

              await postGoogleLogin(
                message.payload.idToken,
                message.payload.serverAuthCode,
              );

              window.location.reload();

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'google',
                },
              });
            } catch (error) {
              console.error('Google 로그인 실패:', error);
              sendMessageToRN({
                type: 'LOGIN_FAILED',
                payload: {
                  error: 'Google 로그인 처리 중 오류가 발생했습니다.',
                  provider: 'google',
                },
              });
            }
            break;

          case 'APPLE_LOGIN_TOKEN':
            console.log('Apple 로그인 토큰 수신:', message.payload);
            try {
              if (!message.payload?.idToken || !message.payload?.serverAuthCode)
                return;

              alert(JSON.stringify(message.payload));

              await postAppleLogin(
                message.payload.idToken,
                message.payload.serverAuthCode,
              );

              window.location.reload();

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'apple',
                },
              });
            } catch (error) {
              console.error('Apple 로그인 실패:', error);
              sendMessageToRN({
                type: 'LOGIN_FAILED',
                payload: {
                  error: 'Apple 로그인 처리 중 오류가 발생했습니다.',
                  provider: 'apple',
                },
              });
            }
            break;

          default:
            console.warn('알 수 없는 WebView 메시지 타입:', message.type);
        }
      },
    );

    return cleanup;
  }, []);

  // 이 컴포넌트는 UI를 렌더링하지 않습니다
  return null;
};
