import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import WebView from 'react-native-webview';

import { DEV_WEB_URL } from '@/constants/env';
import { WEBVIEW_USER_AGENT } from '@/constants/webview';
import { useAuth } from '../../contexts/AuthContext';
import { useWebView } from '../../contexts/WebViewContext';
import { WebToRNMessage } from '../../types/webview';
import { LoginScreenOverlay } from '../login/LoginScreenOverlay';

export const MainScreen = () => {
  const { showWebViewLogin, setShowWebViewLogin } = useAuth();
  const { webViewRef } = useWebView();

  const handleWebViewMessage = (event: any) => {
    try {
      const message: WebToRNMessage = JSON.parse(event.nativeEvent.data);
      console.log('WebView에서 메시지 수신:', message);

      switch (message.type) {
        case 'SHOW_LOGIN_SCREEN':
          setShowWebViewLogin(true);
          break;

        case 'LOGIN_SUCCESS':
          console.log('웹뷰에서 로그인 성공 알림 수신:', message.payload);
          setShowWebViewLogin(false);
          break;

        case 'LOGIN_FAILED':
          console.log('웹뷰에서 로그인 실패 알림 수신:', message.payload);
          setShowWebViewLogin(false);
          break;

        default:
          console.warn('알 수 없는 메시지 타입:', message.type);
      }
    } catch (error) {
      console.error('WebView 메시지 파싱 실패:', error);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.webViewContainer}>
        <WebView
          ref={webViewRef}
          source={{ uri: DEV_WEB_URL }}
          style={styles.webView}
          userAgent={WEBVIEW_USER_AGENT}
          allowsBackForwardNavigationGestures
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          webviewDebuggingEnabled
          domStorageEnabled
          startInLoadingState
          pullToRefreshEnabled
          originWhitelist={['*']}
          onMessage={handleWebViewMessage}
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
          renderLoading={() => (
            <View style={styles.loadingContainer}>
              <View style={styles.loadingIcon}>
                <Ionicons name="sparkles" size={32} color="#FE5E04" />
              </View>
              <Text style={styles.loadingText}>봄봄을 불러오는 중...</Text>
            </View>
          )}
        />
      </View>

      <LoginScreenOverlay
        visible={showWebViewLogin}
        onClose={() => setShowWebViewLogin(false)}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5EA',
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  iconContainer: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#FE5E04',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1C1C1E',
  },
  profileButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
    borderRadius: 8,
    backgroundColor: '#F2F2F7',
    gap: 8,
    maxWidth: 200,
  },
  userInfo: {
    flex: 1,
  },
  userName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1C1C1E',
    marginBottom: 2,
  },
  providerInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  providerText: {
    fontSize: 12,
    color: '#8E8E93',
  },
  webViewContainer: {
    flex: 1,
  },
  webView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  loadingIcon: {
    marginBottom: 16,
  },
  loadingText: {
    fontSize: 16,
    color: '#8E8E93',
    fontWeight: '500',
  },
});
