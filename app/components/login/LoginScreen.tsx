import styled from '@emotion/native';
import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import {
  Dimensions,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppleIcon } from '@/components/icons/AppleIcon';
import { GoogleIcon } from '@/components/icons/GoogleIcon';
import { useAuth } from '@/contexts/AuthContext';
import { useWebView } from '@/contexts/WebViewContext';
import { loginWithApple, loginWithGoogle } from '@/utils/auth';

export const LoginScreen = () => {
  const { showLogin } = useAuth();
  const { sendMessageToWeb } = useWebView();

  const handleGoogleLogin = async () => {
    try {
      await loginWithGoogle(({ identityToken, authorizationCode }) => {
        showLogin();
        sendMessageToWeb({
          type: 'GOOGLE_LOGIN_TOKEN',
          payload: {
            identityToken,
            authorizationCode,
          },
        });
      });
    } catch (error) {
      console.error('Google 로그인 실패:', error);
    }
  };

  const handleAppleLogin = async () => {
    try {
      await loginWithApple(({ identityToken, authorizationCode }) => {
        showLogin();
        sendMessageToWeb({
          type: 'APPLE_LOGIN_TOKEN',
          payload: {
            identityToken,
            authorizationCode,
          },
        });
      });
    } catch (error) {
      console.error('Apple 로그인 실패:', error);
    }
  };

  return (
    <Container>
      <StyledKeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <StyledScrollView
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
          contentContainerStyle={scrollViewContentStyle}
        >
          <MainCard>
            <GreetingWrapper>
              <IconContainer>
                <Ionicons name="sparkles" size={24} color="#FFFFFF" />
              </IconContainer>
              <Title>봄봄에 오신 걸 환영해요</Title>
              <Subtitle>
                당신의 하루에 찾아오는 작은 설렘{'\n'}뉴스레터를 한 곳에서 쉽게
                관리하세요
              </Subtitle>
            </GreetingWrapper>

            <Divider />

            <LoginButton onPress={handleGoogleLogin}>
              <GoogleIcon width={24} height={24} />
              <LoginButtonText>Google로 시작하기</LoginButtonText>
            </LoginButton>

            {Platform.OS === 'ios' && (
              <LoginButton onPress={handleAppleLogin}>
                <AppleIcon width={24} height={24} />
                <LoginButtonText>Apple로 시작하기</LoginButtonText>
              </LoginButton>
            )}

            <TermsText>
              로그인하시면 봄봄의 <TermsLink>서비스 약관</TermsLink>과{' '}
              <TermsLink>개인정보 처리방침</TermsLink>에{'\n'}
              동의하는 것으로 간주됩니다.
            </TermsText>
          </MainCard>
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
  flex: 1;
`;

const scrollViewContentStyle = {
  flexGrow: 1,
  paddingHorizontal: 28,
  paddingVertical: 28,
  justifyContent: 'center' as const,
  minHeight: Dimensions.get('window').height - 100,
};

const MainCard = styled.View`
  width: 100%;
  max-width: 420px;
  align-self: center;
  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const GreetingWrapper = styled.View`
  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const IconContainer = styled.View`
  padding: 18px;
  border-radius: 50px;
  justify-content: center;
  align-items: center;
  background-color: #fe5e04;
  shadowColor: #ffd6c2;
  shadowOffset: 0px 20px;
  shadowOpacity: 0.25;
  shadowRadius: 25px;
  elevation: 20;
`;

const Title = styled.Text`
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  color: #181818;
  background-color: transparent;
`;

const Subtitle = styled.Text`
  margin: 34px 0;
  color: #5c5c5c;
  font-size: 18px;
  font-weight: 400;
  text-align: center;
  line-height: 28px;
`;

const Divider = styled.View`
  width: 100%;
  height: 2px;
  margin-bottom: 34px;
  background-color: #ededed;
  opacity: 0.3;
`;

const LoginButton = styled.TouchableOpacity`
  width: 100%;
  padding: 12px;
  border: 2px solid #ededed;
  border-radius: 8px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  background-color: #ffffff;
  shadow-color: #000000;
  shadow-offset: 0px 4px;
  shadow-opacity: 0.05;
  shadow-radius: 6px;
  elevation: 2;
`;

const LoginButtonText = styled.Text`
  font-size: 16px;
  font-weight: 400;
  color: #000000;
  text-align: center;
`;

const TermsText = styled.Text`
  color: #747474;
  font-size: 12px;
  text-align: center;
  line-height: 18px;
`;

const TermsLink = styled.Text`
  color: #fe5e04;
`;
