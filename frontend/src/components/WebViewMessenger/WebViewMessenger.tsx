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
          case 'LOGIN_SUCCESS':
            console.log('로그인 성공 메시지 수신:', message.payload);
            // 페이지 새로고침으로 인증 상태 업데이트
            if (message.payload?.isAuthenticated) {
              window.location.reload();
            }
            break;

          case 'LOGOUT_SUCCESS':
            console.log('로그아웃 성공 메시지 수신:', message.payload);
            // 페이지 새로고침으로 인증 상태 업데이트
            window.location.reload();
            break;

          case 'AUTH_STATE_CHANGED':
            console.log('인증 상태 변경 메시지 수신:', message.payload);
            // 필요 시 특정 처리 로직 추가
            break;

          case 'GOOGLE_LOGIN_TOKEN':
            console.log('Google 로그인 토큰 수신:', message.payload);
            try {
              if (message.payload?.idToken) {
                await postGoogleLogin(
                  message.payload.idToken,
                  message.payload.serverAuthCode,
                );

                // 로그인 성공을 앱에 알림
                // sendMessageToRN({
                //   type: 'LOGIN_SUCCESS',
                //   payload: {
                //     isAuthenticated: true,
                //     provider: 'google',
                //   },
                // });

                // 페이지 새로고침으로 인증 상태 업데이트
                // window.location.reload();
              }
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
              if (
                message.payload?.identityToken &&
                message.payload?.authorizationCode
              ) {
                await postAppleLogin(
                  message.payload.identityToken,
                  message.payload.authorizationCode,
                );

                // 로그인 성공을 앱에 알림
                sendMessageToRN({
                  type: 'LOGIN_SUCCESS',
                  payload: {
                    isAuthenticated: true,
                    provider: 'apple',
                  },
                });

                // 페이지 새로고침으로 인증 상태 업데이트
                window.location.reload();
              }
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
