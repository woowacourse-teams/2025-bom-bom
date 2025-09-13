import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';

const LoginButton = () => {
  const navigate = useNavigate();

  return (
    <Button
      text="로그인"
      onClick={() => {
        navigate({ to: '/login' });
      }}
    />
  );
};

export default LoginButton;
