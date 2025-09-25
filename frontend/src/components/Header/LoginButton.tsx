import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import {
  isIOS,
  isRunningInWebView,
  sendMessageToRN,
} from '@/libs/webview/webview.utils';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    if (isRunningInWebView() && isIOS())
      sendMessageToRN({
        type: 'SHOW_LOGIN_SCREEN',
      });
    else navigate({ to: '/login' });

    trackEvent({
      category: 'Navigation',
      action: '로그인 버튼 클릭',
      label: 'Header Login Button',
    });
  };

  return <Button text="로그인" onClick={handleLoginClick} />;
};

export default LoginButton;
