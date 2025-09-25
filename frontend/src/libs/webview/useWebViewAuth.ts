import { useNavigate } from '@tanstack/react-router';
import { useEffect } from 'react';
import { RNToWebMessage } from './webview.types';
import {
  addWebViewMessageListener,
  isRunningInWebView,
  sendMessageToRN,
} from './webview.utils';
import { postAppleLogin, postGoogleLogin } from '@/apis/auth';
import { logger } from '@/utils/logger';

export const useWebViewAuth = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isRunningInWebView()) return;

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

              if (!response) return;
              if (!response.isRegistered) {
                navigate({ to: '/signup' });
                return;
              }

              window.location.reload();

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'google',
                },
              });
            } catch (error) {
              logger.error('Google 로그인 실패:', error);
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
              const response = await postAppleLogin({
                identityToken: message.payload.identityToken,
                authorizationCode: message.payload.authorizationCode,
              });

              if (!response) return;
              if (!response.isRegistered) {
                navigate({ to: '/signup' });
                return;
              }

              window.location.reload();

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'apple',
                },
              });
            } catch (error) {
              logger.error('Apple 로그인 실패:', error);
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
            logger.warn('알 수 없는 WebView 메시지 타입:', message.type);
        }
      },
    );

    return cleanup;
  }, [navigate]);
};
