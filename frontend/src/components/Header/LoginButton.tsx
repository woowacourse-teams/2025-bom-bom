import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import {
  isRunningInWebView,
  sendMessageToRN,
} from '@/libs/webview/webview.utils';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    if (isRunningInWebView())
      sendMessageToRN({
        type: 'SHOW_LOGIN_SCREEN',
      });
    else navigate({ to: '/login' });
  };

  return <Button text="로그인" onClick={handleLoginClick} />;
};

export default LoginButton;
