import { Ionicons } from "@expo/vector-icons";
import React, { useCallback, useRef } from "react";
import { Alert, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import WebView from "react-native-webview";

import { useAuth } from "../contexts/AuthContext";
import { RNToWebMessage, WebToRNMessage } from "../types/webview";
import { LoginScreenOverlay } from "./LoginScreenOverlay";

export const MainScreen: React.FC = () => {
  const { user, logout, isLoading, showWebViewLogin, setShowWebViewLogin } =
    useAuth();
  const webViewRef = useRef<WebView>(null);

  // WebView에서 온 메시지 처리
  const handleWebViewMessage = (event: any) => {
    try {
      const message: WebToRNMessage = JSON.parse(event.nativeEvent.data);
      console.log("WebView에서 메시지 수신:", message);

      switch (message.type) {
        case "SHOW_LOGIN_SCREEN":
          setShowWebViewLogin(true);
          break;
        case "LOGOUT_REQUEST":
          handleWebViewLogout();
          break;
        default:
          console.warn("알 수 없는 메시지 타입:", message.type);
      }
    } catch (error) {
      console.error("WebView 메시지 파싱 실패:", error);
    }
  };

  // WebView에서 로그아웃 요청 시 처리
  const handleWebViewLogout = () => {
    Alert.alert("로그아웃", "정말 로그아웃하시겠습니까?", [
      {
        text: "취소",
        style: "cancel",
      },
      {
        text: "로그아웃",
        style: "destructive",
        onPress: async () => {
          try {
            await logout();
            sendMessageToWeb({
              type: "LOGOUT_SUCCESS",
              payload: { isAuthenticated: false },
            });
          } catch (error) {
            console.error("로그아웃 실패:", error);
          }
        },
      },
    ]);
  };

  // WebView로 메시지 전송
  const sendMessageToWeb = (message: RNToWebMessage) => {
    try {
      const messageString = JSON.stringify(message);
      webViewRef.current?.postMessage(messageString);
      console.log("WebView로 메시지 전송:", message);
    } catch (error) {
      console.error("WebView 메시지 전송 실패:", error);
    }
  };

  // 로그인 성공 시 WebView로 알림
  const handleLoginSuccess = useCallback(() => {
    setShowWebViewLogin(false);
    sendMessageToWeb({
      type: "LOGIN_SUCCESS",
      payload: {
        user: user
          ? {
              id: user.id,
              email: user.email,
              name: user.name,
              provider: user.provider,
            }
          : undefined,
        isAuthenticated: !!user,
      },
    });
  }, [user, setShowWebViewLogin]);

  // 로그인 성공 감지
  React.useEffect(() => {
    if (user && showWebViewLogin) {
      handleLoginSuccess();
    }
  }, [user, showWebViewLogin, handleLoginSuccess]);

  const getProviderIcon = (provider: string) => {
    switch (provider) {
      case "google":
        return "logo-google";
      case "apple":
        return "logo-apple";
      default:
        return "mail";
    }
  };

  const getProviderName = (provider: string) => {
    switch (provider) {
      case "google":
        return "Google";
      case "apple":
        return "Apple";
      default:
        return "이메일";
    }
  };

  const handleLogout = () => {
    Alert.alert("로그아웃", "정말 로그아웃하시겠습니까?", [
      {
        text: "취소",
        style: "cancel",
      },
      {
        text: "로그아웃",
        style: "destructive",
        onPress: logout,
      },
    ]);
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* 웹뷰 */}
      <View style={styles.webViewContainer}>
        <WebView
          ref={webViewRef}
          source={{ uri: "http://localhost:3000" }}
          style={styles.webView}
          userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/96.0.4664.116 Mobile/15E148 Safari/604.1"
          allowsBackForwardNavigationGestures
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          webviewDebuggingEnabled
          pullToRefreshEnabled
          onMessage={handleWebViewMessage}
          onContentProcessDidTerminate={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.warn("WebView Content Process Did Terminate:", nativeEvent);
            webViewRef.current?.reload();
          }}
          onError={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.error("WebView Error:", nativeEvent);
          }}
          onHttpError={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.error("WebView HTTP Error:", nativeEvent);
          }}
          startInLoadingState
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

      {/* 웹뷰 로그인 오버레이 */}
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
    backgroundColor: "#FFFFFF",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: "#FFFFFF",
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5EA",
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
    flex: 1,
  },
  iconContainer: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: "#FE5E04",
    justifyContent: "center",
    alignItems: "center",
    marginRight: 8,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "700",
    color: "#1C1C1E",
  },
  profileButton: {
    flexDirection: "row",
    alignItems: "center",
    padding: 8,
    borderRadius: 8,
    backgroundColor: "#F2F2F7",
    gap: 8,
    maxWidth: 200,
  },
  userInfo: {
    flex: 1,
  },
  userName: {
    fontSize: 14,
    fontWeight: "600",
    color: "#1C1C1E",
    marginBottom: 2,
  },
  providerInfo: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
  },
  providerText: {
    fontSize: 12,
    color: "#8E8E93",
  },
  webViewContainer: {
    flex: 1,
  },
  webView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#FFFFFF",
  },
  loadingIcon: {
    marginBottom: 16,
  },
  loadingText: {
    fontSize: 16,
    color: "#8E8E93",
    fontWeight: "500",
  },
});
