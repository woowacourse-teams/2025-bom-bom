import { useMutation } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { postSignup } from '@/apis/auth';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { sendMessageToRN } from '@/libs/webview/webview.utils';
import type { Gender } from '../components/SignupCard.types';

export const useSignupMutation = ({
  nickname,
  email,
  gender,
  birthDate,
}: {
  nickname: string;
  email: string;
  gender: Gender;
  birthDate: string;
}) => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: () =>
      postSignup({
        nickname: nickname.trim(),
        email,
        gender,
        birthDate,
      }),
    onSuccess: () => {
      sendMessageToRN({
        type: 'LOGIN_SUCCESS',
      });
      navigate({ to: '/today' });
      trackEvent({
        category: 'Authentication',
        action: '회원가입 성공',
        label: '회원가입 완료 후 메인 페이지 이동',
      });
    },
    onError: (e) => {
      const errorMessage = e.message;
      trackEvent({
        category: 'Authentication',
        action: '회원가입 실패',
        label: `회원가입 실패 에러메시지: ${errorMessage}`,
      });
    },
  });
};
