import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';

const LoginButton = () => {
  const navigate = useNavigate();

  return (
    <Button
      text="로그인"
      onClick={() => {
        navigate({ to: '/login' });
        trackEvent({
          category: 'Navigation',
          action: '로그인 버튼 클릭',
          label: 'Header Login Button',
        });
      }}
    />
  );
};

export default LoginButton;
