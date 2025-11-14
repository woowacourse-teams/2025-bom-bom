import { activateKeepAwakeAsync, deactivateKeepAwake } from 'expo-keep-awake';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import { Slot } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { Platform } from 'react-native';
import { AuthProvider } from '../contexts/AuthContext';
import { EmotionThemeProvider } from '../contexts/ThemeContext';
import { WebViewProvider } from '../contexts/WebViewContext';

SplashScreen.setOptions({
  duration: 1_500,
  fade: true,
});

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
