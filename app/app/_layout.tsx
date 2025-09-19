import { StatusBar } from 'expo-status-bar';
import React from 'react';
import 'react-native-reanimated';

import { MainScreen } from '../components/MainScreen';
import { AuthProvider } from '../contexts/AuthContext';
import { WebViewProvider } from '../contexts/WebViewContext';

export default function RootLayout() {
  return (
    <AuthProvider>
      <WebViewProvider>
        <StatusBar style="auto" />
        <MainScreen />
      </WebViewProvider>
    </AuthProvider>
  );
}
