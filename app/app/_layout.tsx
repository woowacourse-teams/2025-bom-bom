import { StatusBar } from 'expo-status-bar';
import React from 'react';

import { Slot } from 'expo-router';
import { AuthProvider } from '../contexts/AuthContext';
import { EmotionThemeProvider } from '../contexts/ThemeContext';
import { WebViewProvider } from '../contexts/WebViewContext';

export default function RootLayout() {
  return (
    <EmotionThemeProvider>
      <WebViewProvider>
        <AuthProvider>
          <StatusBar style="auto" />
          <Slot />
        </AuthProvider>
      </WebViewProvider>
    </EmotionThemeProvider>
  );
}
