import styled from '@emotion/native';
import { Ionicons } from '@expo/vector-icons';
import React, { useEffect } from 'react';
import {
  Dimensions,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Button } from '@/components/common/Button';
import { useAuth } from '@/contexts/AuthContext';
import { useWebView } from '@/contexts/WebViewContext';
import { loginWithApple, loginWithGoogle } from '@/utils/auth';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const LoginScreen = () => {
  const { setShowWebViewLogin } = useAuth();
  const { sendMessageToWeb } = useWebView();

  const handleGoogleLogin = async () => {
    try {
      await loginWithGoogle(() => setShowWebViewLogin(true));
    } catch (error) {
      console.error('Google 로그인 실패:', error);
    }
  };

  const handleAppleLogin = async () => {
    try {
      await loginWithApple(() => setShowWebViewLogin(true));
    } catch (error) {
      console.error('Apple 로그인 실패:', error);
    }
  };

  useEffect(() => {
    const sendLoginTokenToWeb = async () => {
      const auth = await AsyncStorage.getItem('auth');
      if (!auth) return;

      const { provider, identityToken, authorizationCode } = JSON.parse(auth);

      if (!authorizationCode || !identityToken || !provider) return;

      sendMessageToWeb({
        type:
          provider.toUpperCase() === 'GOOGLE'
            ? 'GOOGLE_LOGIN_TOKEN'
            : 'APPLE_LOGIN_TOKEN',
        payload: {
          identityToken,
          authorizationCode,
        },
      });

      AsyncStorage.removeItem('auth');

      setShowWebViewLogin(false);
    };

    sendLoginTokenToWeb();
  }, [sendMessageToWeb, setShowWebViewLogin]);

  return (
    <Container>
      <StyledKeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <StyledScrollView
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          <Header>
            <IconContainer>
              <Ionicons name="sparkles" size={24} color="#FFFFFF" />
            </IconContainer>
            <Title>봄봄에 오신 걸 환영해요</Title>
            <Subtitle>
              당신의 하루에 찾아오는 작은 설렘{'\n'}뉴스레터를 한 곳에서 쉽게
              관리하세요
            </Subtitle>
          </Header>

          <Divider />

          <LoginSection>
            <SocialLogin>
              <Button
                title="Google로 시작하기"
                onPress={handleGoogleLogin}
                variant="social"
                icon={<Ionicons name="logo-google" size={24} color="#4285F4" />}
              />

              {Platform.OS === 'ios' && (
                <Button
                  title="Apple로 시작하기"
                  onPress={handleAppleLogin}
                  variant="social"
                  icon={
                    <Ionicons name="logo-apple" size={24} color="#000000" />
                  }
                />
              )}
            </SocialLogin>
          </LoginSection>

          <TermsContainer>
            <TermsText>
              로그인하시면 봄봄의 <TermsLink>서비스 약관</TermsLink>과{' '}
              <TermsLink>개인정보 처리방침</TermsLink>에{'\n'}
              동의하는 것으로 간주됩니다.
            </TermsText>
          </TermsContainer>
        </StyledScrollView>
      </StyledKeyboardAvoidingView>
    </Container>
  );
};

const Container = styled(SafeAreaView)`
  flex: 1;
  background-color: #ffffff;
`;

const StyledKeyboardAvoidingView = styled(KeyboardAvoidingView)`
  flex: 1;
`;

const StyledScrollView = styled(ScrollView)`
  flex-grow: 1;

  min-height: ${Dimensions.get('window').height - 100}px;
  gap: 16px;
`;

const Header = styled.View`
  align-items: center;
  gap: 20px;
`;

const IconContainer = styled.View`
  width: 60px;
  height: 60px;
  border-radius: 30px;
  background-color: #fe5e04;
  justify-content: center;
  align-items: center;
  shadow-color: #ffd6c2;
  shadow-offset: 0px 20px;
  shadow-opacity: 0.4;
  shadow-radius: 25px;
  elevation: 12;
`;

const Title = styled.Text`
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  color: #181818;
`;

const Subtitle = styled.Text`
  font-size: 18px;
  font-weight: 400;
  color: #5c5c5c;
  text-align: center;
  line-height: 28px;
  margin-horizontal: 24px;
`;

const Divider = styled.View`
  height: 2px;
  margin-bottom: 34px;
  background-color: transparent;
  border-bottom-width: 1px;
  border-bottom-color: #ededed;
  opacity: 0.5;
  margin-horizontal: -28px;
`;

const LoginSection = styled.View`
  gap: 16px;
`;

const SocialLogin = styled.View`
  gap: 16px;
`;

const TermsContainer = styled.View`
  margin-top: auto;
  padding-top: 24px;
`;

const TermsText = styled.Text`
  font-size: 12px;
  color: #747474;
  text-align: center;
  line-height: 18px;
`;

const TermsLink = styled.Text`
  color: #fe5e04;
  font-weight: 600;
`;
