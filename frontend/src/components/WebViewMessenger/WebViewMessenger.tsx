import { useEffect } from 'react';
import {
  addWebViewMessageListener,
  RNToWebMessage,
} from '@/utils/webviewBridge';

/**
 * WebView와 React Native 간의 메시지를 수신하고 처리하는 컴포넌트
 * 앱의 최상위 레벨에서 사용되어야 합니다.
 */
export const WebViewMessenger: React.FC = () => {
  useEffect(() => {
    // React Native에서 온 메시지를 수신하는 리스너 등록
    const cleanup = addWebViewMessageListener((message: RNToWebMessage) => {
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

        default:
          console.warn('알 수 없는 WebView 메시지 타입:', message.type);
      }
    });

    return cleanup;
  }, []);

  // 이 컴포넌트는 UI를 렌더링하지 않습니다
  return null;
};
