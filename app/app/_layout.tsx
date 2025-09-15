import React from "react";
import "react-native-reanimated";
import { StatusBar } from "expo-status-bar";

import { AuthProvider, useAuth } from "../contexts/AuthContext";
import { SplashScreen } from "../components/SplashScreen";
import { LoginScreen } from "../components/LoginScreen";
import { MainScreen } from "../components/MainScreen";

function AppContent() {
  const { user, isLoading, isAuthenticated } = useAuth();

  // 로딩 중일 때 스플래시 화면 표시
  if (isLoading) {
    return <SplashScreen />;
  }

  // 인증되지 않은 경우 로그인 화면 표시
  if (!isAuthenticated) {
    return <LoginScreen />;
  }

  // 인증된 경우 메인 화면 표시
  return <MainScreen />;
}

export default function RootLayout() {
  return (
    <AuthProvider>
      <StatusBar style="auto" />
      <AppContent />
    </AuthProvider>
  );
}
