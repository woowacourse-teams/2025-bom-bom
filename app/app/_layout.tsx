import { StatusBar } from 'expo-status-bar';
import React from 'react';
import 'react-native-reanimated';

import { MainScreen } from '../components/main/MainScreen';
import { AuthProvider } from '../contexts/AuthContext';
import { WebViewProvider } from '../contexts/WebViewContext';

export default function RootLayout() {
  return (
    <WebViewProvider>
      <AuthProvider>
        <StatusBar style="auto" />
        <MainScreen />
      </AuthProvider>
    </WebViewProvider>
  );
}
