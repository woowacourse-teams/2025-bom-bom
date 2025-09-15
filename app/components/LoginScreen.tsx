import { Ionicons } from "@expo/vector-icons";
import React, { useState } from "react";
import {
  Alert,
  Dimensions,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { useAuth } from "../contexts/AuthContext";
import { Button } from "./Button";
import { Input } from "./Input";

const { width } = Dimensions.get("window");

export const LoginScreen: React.FC = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isEmailLogin, setIsEmailLogin] = useState(false);
  const [emailError, setEmailError] = useState("");
  const [passwordError, setPasswordError] = useState("");

  const {
    login,
    loginWithGoogle,
    loginWithApple,
    isLoading,
    error,
    clearError,
  } = useAuth();

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^s@]+@[^s@]+.[^s@]+$/;
    return emailRegex.test(email);
  };

  const handleEmailLogin = async () => {
    // 입력값 검증
    setEmailError("");
    setPasswordError("");
    clearError();

    if (!email) {
      setEmailError("이메일을 입력해주세요.");
      return;
    }

    if (!validateEmail(email)) {
      setEmailError("올바른 이메일 주소를 입력해주세요.");
      return;
    }

    if (!password) {
      setPasswordError("비밀번호를 입력해주세요.");
      return;
    }

    if (password.length < 6) {
      setPasswordError("비밀번호는 6자 이상 입력해주세요.");
      return;
    }

    try {
      await login(email, password);
    } catch (err) {
      console.error("Email login error:", err);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      clearError();
      await loginWithGoogle();
    } catch (err) {
      console.error("Google login error:", err);
    }
  };

  const handleAppleLogin = async () => {
    try {
      clearError();
      await loginWithApple();
    } catch (err) {
      console.error("Apple login error:", err);
    }
  };

  const showErrorAlert = () => {
    if (error) {
      Alert.alert("로그인 오류", error);
    }
  };

  React.useEffect(() => {
    if (error) {
      showErrorAlert();
    }
  }, [error]);

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        style={styles.keyboardAvoidingView}
      >
        <ScrollView
          contentContainerStyle={styles.scrollContainer}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* 헤더 섹션 */}
          <View style={styles.header}>
            <View style={styles.iconContainer}>
              <Ionicons name="sparkles" size={24} color="#FFFFFF" />
            </View>
            <Text style={styles.title}>봄봄에 오신 걸 환영해요</Text>
            <Text style={styles.subtitle}>
              당신의 하루에 찾아오는 작은 설렘{"n"}뉴스레터를 한 곳에서 쉽게
              관리하세요
            </Text>
          </View>

          {/* 구분선 */}
          <View style={styles.divider} />

          {/* 로그인 섹션 */}
          <View style={styles.loginSection}>
            {/* 이메일 로그인 폼 */}
            {isEmailLogin && (
              <View style={styles.emailForm}>
                <Input
                  label="이메일"
                  value={email}
                  onChangeText={setEmail}
                  placeholder="이메일을 입력하세요"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  leftIcon="mail"
                  error={emailError}
                />
                <Input
                  label="비밀번호"
                  value={password}
                  onChangeText={setPassword}
                  placeholder="비밀번호를 입력하세요"
                  isPassword
                  leftIcon="lock-closed"
                  error={passwordError}
                />
                <Button
                  title="로그인"
                  onPress={handleEmailLogin}
                  loading={isLoading}
                  style={styles.loginButton}
                />
                <Button
                  title="다른 방법으로 로그인"
                  onPress={() => setIsEmailLogin(false)}
                  variant="secondary"
                  style={styles.switchButton}
                />
              </View>
            )}

            {/* 소셜 로그인 버튼들 */}
            {!isEmailLogin && (
              <View style={styles.socialLogin}>
                {/* Google 로그인 */}
                <Button
                  title="Google로 시작하기"
                  onPress={handleGoogleLogin}
                  variant="social"
                  loading={isLoading}
                  icon={
                    <Ionicons name="logo-google" size={24} color="#4285F4" />
                  }
                  style={styles.socialButton}
                />

                {/* Apple 로그인 (iOS에서만 표시) */}
                {Platform.OS === "ios" && (
                  <Button
                    title="Apple로 시작하기"
                    onPress={handleAppleLogin}
                    variant="social"
                    loading={isLoading}
                    icon={
                      <Ionicons name="logo-apple" size={24} color="#000000" />
                    }
                    style={styles.socialButton}
                  />
                )}
              </View>
            )}
          </View>

          {/* 약관 안내 */}
          <View style={styles.termsContainer}>
            <Text style={styles.termsText}>
              로그인하시면 봄봄의{" "}
              <Text style={styles.termsLink}>서비스 약관</Text>과{" "}
              <Text style={styles.termsLink}>개인정보 처리방침</Text>에{"n"}
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
    backgroundColor: "#FFFFFF",
  },
  keyboardAvoidingView: {
    flex: 1,
  },
  scrollContainer: {
    flexGrow: 1,
    paddingHorizontal: 28,
    justifyContent: "center",
    minHeight: Dimensions.get("window").height - 100,
  },
  header: {
    alignItems: "center",
    marginBottom: 32,
  },
  iconContainer: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: "#FE5E04",
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 20,
    shadowColor: "#FE5E04",
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
    fontWeight: "700",
    color: "#1C1C1E",
    textAlign: "center",
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 16,
    color: "#8E8E93",
    textAlign: "center",
    lineHeight: 24,
  },
  divider: {
    height: 2,
    backgroundColor: "#E5E5EA",
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
    width: "100%",
  },
  emailButton: {
    marginTop: 8,
  },
  termsContainer: {
    marginTop: "auto",
    paddingTop: 24,
  },
  termsText: {
    fontSize: 14,
    color: "#8E8E93",
    textAlign: "center",
    lineHeight: 20,
  },
  termsLink: {
    color: "#FE5E04",
    fontWeight: "600",
  },
});
