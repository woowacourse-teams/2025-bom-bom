import { useEffect } from 'react';
import { postGoogleLogin, postAppleLogin } from '@/apis/auth';
import {
  RNToWebMessage,
  addWebViewMessageListener,
  sendMessageToRN,
} from '@/libs/webview';

export const WebViewMessenger: React.FC = () => {
  useEffect(() => {
    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        switch (message.type) {
          case 'GOOGLE_LOGIN_TOKEN':
            if (
              !message.payload?.identityToken ||
              !message.payload?.authorizationCode
            )
              return;

            try {
              const response = await postGoogleLogin({
                identityToken: message.payload.identityToken,
                authorizationCode: message.payload.authorizationCode,
              });

              alert(JSON.stringify(response));

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
            if (
              !message.payload?.identityToken ||
              !message.payload?.authorizationCode
            )
              return;

            try {
              await postAppleLogin({
                identityToken: message.payload.identityToken,
                authorizationCode: message.payload.authorizationCode,
              });

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

  return null;
};
