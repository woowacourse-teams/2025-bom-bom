import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import {
  isRunningInWebView,
  requestShowLoginScreen,
} from '@/utils/webviewBridge';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    // WebView 환경에서는 React Native로 메시지 전송
    if (isRunningInWebView()) {
      requestShowLoginScreen();
    } else {
      // 일반 웹 환경에서는 기존 로직
      navigate({ to: '/login' });
    }
  };

  return <Button text="로그인" onClick={handleLoginClick} />;
};

export default LoginButton;
