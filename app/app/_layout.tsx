import { StatusBar } from 'expo-status-bar';
import React from 'react';
import 'react-native-reanimated';

import { MainScreen } from '../components/MainScreen';
import { AuthProvider } from '../contexts/AuthContext';

export default function RootLayout() {
  return (
    <AuthProvider>
      <StatusBar style="auto" />
      <MainScreen />
    </AuthProvider>
  );
}
