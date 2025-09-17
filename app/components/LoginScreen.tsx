import { Ionicons } from '@expo/vector-icons';
import React, { useEffect } from 'react';
import {
  Dimensions,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import AsyncStorage from '@react-native-async-storage/async-storage';
import { useAuth } from '../contexts/AuthContext';
import { Button } from './Button';

export const LoginScreen = () => {
  const {
    loginWithGoogle,
    loginWithApple,
    sendMessageToWeb,
    setShowWebViewLogin,
  } = useAuth();

  const handleGoogleLogin = async () => {
    try {
      await loginWithGoogle();
    } catch (err) {
      console.error('Google login error:', err);
    }
  };

  const handleAppleLogin = async () => {
    try {
      await loginWithApple();
    } catch (err) {
      console.error('Apple login error:', err);
    }
  };

  useEffect(() => {
    (async () => {
      const auth = await AsyncStorage.getItem('auth');
      if (!auth) return;
      const { provider, identityToken, authorizationCode } = JSON.parse(auth);

      if (!authorizationCode || !identityToken || !provider) {
        return;
      }

      if (provider === 'google') {
        sendMessageToWeb({
          type: 'GOOGLE_LOGIN_TOKEN',
          payload: {
            identityToken,
            authorizationCode,
          },
        });
      } else if (provider === 'apple') {
        sendMessageToWeb({
          type: 'APPLE_LOGIN_TOKEN',
          payload: {
            identityToken,
            authorizationCode,
          },
        });
      }

      AsyncStorage.removeItem('auth');

      setShowWebViewLogin(false);
    })();
  }, [sendMessageToWeb, setShowWebViewLogin]);

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardAvoidingView}
      >
        <ScrollView
          contentContainerStyle={styles.scrollContainer}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          <View style={styles.header}>
            <View style={styles.iconContainer}>
              <Ionicons name="sparkles" size={24} color="#FFFFFF" />
            </View>
            <Text style={styles.title}>봄봄에 오신 걸 환영해요</Text>
            <Text style={styles.subtitle}>
              당신의 하루에 찾아오는 작은 설렘{'\n'}뉴스레터를 한 곳에서 쉽게
              관리하세요
            </Text>
          </View>

          <View style={styles.divider} />

          <View style={styles.loginSection}>
            <View style={styles.socialLogin}>
              <Button
                title="Google로 시작하기"
                onPress={handleGoogleLogin}
                variant="social"
                icon={<Ionicons name="logo-google" size={24} color="#4285F4" />}
                style={styles.socialButton}
              />

              {Platform.OS === 'ios' && (
                <Button
                  title="Apple로 시작하기"
                  onPress={handleAppleLogin}
                  variant="social"
                  icon={
                    <Ionicons name="logo-apple" size={24} color="#000000" />
                  }
                  style={styles.socialButton}
                />
              )}
            </View>
          </View>

          <View style={styles.termsContainer}>
            <Text style={styles.termsText}>
              로그인하시면 봄봄의{' '}
              <Text style={styles.termsLink}>서비스 약관</Text>과{' '}
              <Text style={styles.termsLink}>개인정보 처리방침</Text>에{'n'}
              동의하는 것으로 간주됩니다.
            </Text>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  keyboardAvoidingView: {
    flex: 1,
  },
  scrollContainer: {
    flexGrow: 1,
    paddingHorizontal: 28,
    justifyContent: 'center',
    minHeight: Dimensions.get('window').height - 100,
  },
  header: {
    alignItems: 'center',
    marginBottom: 32,
  },
  iconContainer: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#FE5E04',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#FE5E04',
    shadowOffset: {
      width: 0,
      height: 8,
    },
    shadowOpacity: 0.3,
    shadowRadius: 16,
    elevation: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#1C1C1E',
    textAlign: 'center',
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 16,
    color: '#8E8E93',
    textAlign: 'center',
    lineHeight: 24,
  },
  divider: {
    height: 2,
    backgroundColor: '#E5E5EA',
    marginBottom: 32,
    opacity: 0.5,
  },
  loginSection: {
    marginBottom: 24,
  },
  emailForm: {
    gap: 16,
  },
  socialLogin: {
    gap: 16,
  },
  loginButton: {
    marginTop: 8,
  },
  switchButton: {
    marginTop: 8,
  },
  socialButton: {
    width: '100%',
  },
  emailButton: {
    marginTop: 8,
  },
  termsContainer: {
    marginTop: 'auto',
    paddingTop: 24,
  },
  termsText: {
    fontSize: 14,
    color: '#8E8E93',
    textAlign: 'center',
    lineHeight: 20,
  },
  termsLink: {
    color: '#FE5E04',
    fontWeight: '600',
  },
});
