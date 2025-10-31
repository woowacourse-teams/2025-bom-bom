import styled from '@emotion/native';

import { SafeAreaView } from 'react-native-safe-area-context';
import WebView, { WebViewMessageEvent } from 'react-native-webview';

import { useAuth } from '../../contexts/AuthContext';
import { useWebView } from '../../contexts/WebViewContext';
import useAndroidNavigationState from '../../hooks/useAndroidNavigationState';
import { WebToRNMessage } from '@bombom/shared/webview';
import { LoginScreenOverlay } from '../login/LoginScreenOverlay';

import * as WebBrowser from 'expo-web-browser';

import { ENV } from '@/constants/env';
import { WEBVIEW_USER_AGENT } from '@/constants/webview';

export const MainScreen = () => {
  const { showWebViewLogin, showLogin, hideLogin } = useAuth();
  const { webViewRef } = useWebView();

  const { handleNavigationStateChange } = useAndroidNavigationState();

  const handleWebViewMessage = (event: WebViewMessageEvent) => {
    try {
      const message: WebToRNMessage = JSON.parse(event.nativeEvent.data);
      console.log('WebView에서 메시지 수신:', message);

      switch (message.type) {
        case 'SHOW_LOGIN_SCREEN':
          showLogin();
          break;

        case 'LOGIN_SUCCESS':
          console.log('웹뷰에서 로그인 성공 알림 수신:', message.payload);
          hideLogin();
          break;

        case 'LOGIN_FAILED':
          console.log('웹뷰에서 로그인 실패 알림 수신:', message.payload);
          hideLogin();
          break;

        case 'OPEN_BROWSER':
          if (message.payload?.url) {
            console.log('외부 브라우저 열기:', message.payload.url);
            WebBrowser.openBrowserAsync(message.payload.url, {
              presentationStyle: WebBrowser.WebBrowserPresentationStyle.POPOVER,
              dismissButtonStyle: 'close',
            });
          }
          break;

        default:
          console.log('알수 없는 메시지 수신:', message);
          break;
      }
    } catch (error) {
      console.error('WebView 메시지 파싱 실패:', error);
    }
  };

  return (
    <Container>
      <WebViewContainer>
        <StyledWebView
          ref={webViewRef}
          source={{ uri: ENV.webUrl }}
          userAgent={`${navigator.userAgent} ${WEBVIEW_USER_AGENT}`}
          allowsBackForwardNavigationGestures
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          webviewDebuggingEnabled
          domStorageEnabled
          pullToRefreshEnabled
          originWhitelist={['*']}
          onMessage={handleWebViewMessage}
          onNavigationStateChange={handleNavigationStateChange}
          onContentProcessDidTerminate={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.warn('WebView Content Process Did Terminate:', nativeEvent);
            webViewRef.current?.reload();
          }}
          onError={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.error('WebView Error:', nativeEvent);
          }}
          onHttpError={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.error('WebView HTTP Error:', nativeEvent);
          }}
        />
      </WebViewContainer>

      <LoginScreenOverlay visible={showWebViewLogin} onClose={hideLogin} />
    </Container>
  );
};

const Container = styled(SafeAreaView)`
  flex: 1;
  background-color: ${(props) => props.theme.colors.white};
`;

const WebViewContainer = styled.View`
  flex: 1;
`;

const StyledWebView = styled(WebView)`
  flex: 1;
`;
