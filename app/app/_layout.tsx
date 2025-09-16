import { StatusBar } from "expo-status-bar";
import React from "react";
import "react-native-reanimated";

import { SplashScreen } from "@/components/SplashScreen";
import { MainScreen } from "../components/MainScreen";
import { AuthProvider, useAuth } from "../contexts/AuthContext";

function AppContent() {
  const { isLoading, isAuthenticated } = useAuth();

  if (isLoading) return <SplashScreen />;
  // if (!isAuthenticated) return <LoginScreen />;
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
