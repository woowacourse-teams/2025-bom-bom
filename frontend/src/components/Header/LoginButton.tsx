import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { isRunningInWebView } from '@/libs/webview';
import { requestShowLoginScreen } from '@/utils/webviewBridge';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    if (isRunningInWebView()) requestShowLoginScreen();
    else navigate({ to: '/login' });
  };

  return <Button text="로그인" onClick={handleLoginClick} />;
};

export default LoginButton;
