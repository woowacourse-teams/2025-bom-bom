import { useNavigate } from '@tanstack/react-router';
import { useEffect } from 'react';
import {
  addWebViewMessageListener,
  isWebView,
  sendMessageToRN,
} from './webview.utils';
import { postAppleLogin, postGoogleLogin } from '@/apis/auth';
import { logger } from '@/utils/logger';
import type { RNToWebMessage } from './webview.types';

export const useWebViewAuth = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isWebView()) return;

    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        if (message.type === 'GOOGLE_LOGIN_TOKEN') {
            if (
              !message.payload?.identityToken ||
              !message.payload?.authorizationCode
            )
              return;

            try {
              const response = await postGoogleLogin({
                identityToken: message.payload.identityToken,
                authorizationCode: message.payload.authorizationCode,
                email: message.payload.email ?? '',
                nickname: message.payload.name ?? '',
              });

              if (!response) return;

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'google',
                },
              });

              if (!response.isRegistered) {
                navigate({
                  to: '/signup',
                  search: {
                    email: response.email,
                    name: response.nickname,
                  },
                });
                return;
              }

              window.location.reload();
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
        } else if (message.type === 'APPLE_LOGIN_TOKEN') {
            if (
              !message.payload?.identityToken ||
              !message.payload?.authorizationCode
            )
              return;

            try {
              const response = await postAppleLogin({
                identityToken: message.payload.identityToken,
                authorizationCode: message.payload.authorizationCode,
                email: message.payload.email ?? '',
                nickname: message.payload.name ?? '',
              });

              if (!response) return;

              sendMessageToRN({
                type: 'LOGIN_SUCCESS',
                payload: {
                  isAuthenticated: true,
                  provider: 'apple',
                },
              });

              if (!response.isRegistered) {
                navigate({
                  to: '/signup',
                  search: {
                    email: response.email,
                    name: response.nickname,
                  },
                });
                return;
              }

              window.location.reload();
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
        }
      },
    );

    return cleanup;
  }, [navigate]);
};
