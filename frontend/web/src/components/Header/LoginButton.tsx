import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { isWebView, sendMessageToRN } from '@/libs/webview/webview.utils';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    if (isWebView())
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

  return <Button onClick={handleLoginClick}>로그인</Button>;
};

export default LoginButton;
