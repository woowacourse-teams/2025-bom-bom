import { activateKeepAwakeAsync, deactivateKeepAwake } from 'expo-keep-awake';
import { StatusBar } from 'expo-status-bar';
import React, { useEffect } from 'react';

import { Slot } from 'expo-router';
import { Platform } from 'react-native';
import { AuthProvider } from '../contexts/AuthContext';
import { EmotionThemeProvider } from '../contexts/ThemeContext';
import { WebViewProvider } from '../contexts/WebViewContext';

export default function RootLayout() {
  useEffect(() => {
    if (Platform.OS === 'web') return;

    activateKeepAwakeAsync();

    return () => {
      deactivateKeepAwake();
    };
  }, []);

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
