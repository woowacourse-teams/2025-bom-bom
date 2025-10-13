import styled from '@emotion/styled';
import { useMutation } from '@tanstack/react-query';
import { postWithdraw } from '@/apis/auth';
import { toast } from '@/components/Toast/utils/toastActions';
import type { Device } from '@/hooks/useDevice';

interface WithdrawSectionProps {
  device: Device;
}

const WithdrawSection = ({ device }: WithdrawSectionProps) => {
  const { mutate: mutateWithdraw } = useMutation({
    mutationKey: ['withdraw'],
    mutationFn: postWithdraw,
    onSuccess: () => {
      window.location.reload();
    },
    onError: () => {
      toast.error('회원탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });

  const handleWithdrawClick = () => {
    const confirmWithdraw = confirm(
      '회원 탈퇴 시, 회원님의 모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?',
    );

    if (confirmWithdraw) {
      mutateWithdraw();
    }
  };

  return (
    <Section device={device}>
      <WithdrawButton type="button" onClick={handleWithdrawClick}>
        회원 탈퇴
      </WithdrawButton>
    </Section>
  );
};

export default WithdrawSection;

const Section = styled.section<{ device: Device }>`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const WithdrawButton = styled.button`
  width: fit-content;

  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};

  &:hover {
    text-decoration: underline;
  }
`;
