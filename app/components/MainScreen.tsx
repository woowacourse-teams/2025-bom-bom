import { Ionicons } from "@expo/vector-icons";
import React from "react";
import { Alert, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import WebView from "react-native-webview";

import { useAuth } from "../contexts/AuthContext";

export const MainScreen: React.FC = () => {
  const { user, logout, isLoading } = useAuth();

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

  return (
    <SafeAreaView style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <View style={styles.iconContainer}>
            <Ionicons name="sparkles" size={20} color="#FFFFFF" />
          </View>
          <Text style={styles.headerTitle}>봄봄</Text>
        </View>

        <TouchableOpacity
          style={styles.profileButton}
          onPress={handleLogout}
          disabled={isLoading}
        >
          <View style={styles.userInfo}>
            <Text style={styles.userName} numberOfLines={1}>
              {user?.name || user?.email}
            </Text>
            <View style={styles.providerInfo}>
              <Ionicons
                name={getProviderIcon(user?.provider || "email")}
                size={12}
                color="#8E8E93"
              />
              <Text style={styles.providerText}>
                {getProviderName(user?.provider || "email")}
              </Text>
            </View>
          </View>
          <Ionicons name="log-out-outline" size={20} color="#8E8E93" />
        </TouchableOpacity>
      </View>

      {/* 웹뷰 */}
      <View style={styles.webViewContainer}>
        <WebView
          source={{ uri: "https://www.bombom.news" }}
          style={styles.webView}
          userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/96.0.4664.116 Mobile/15E148 Safari/604.1"
          allowsBackForwardNavigationGestures
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          webviewDebuggingEnabled
          pullToRefreshEnabled
          onContentProcessDidTerminate={(syntheticEvent) => {
            const { nativeEvent } = syntheticEvent;
            console.warn("WebView Content Process Did Terminate:", nativeEvent);
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
