import styled from '@emotion/styled';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { patchNickname } from '@/apis/members';
import Button from '@/components/Button/Button';
import InputField from '@/components/InputField/InputField';
import { toast } from '@/components/Toast/utils/toastActions';
import type { UserInfo } from '@/types/me';

interface NicknameSectionProps {
  userInfo: UserInfo;
}

const NicknameSection = ({ userInfo }: NicknameSectionProps) => {
  const queryClient = useQueryClient();

  const [nickname, setNickname] = useState(userInfo?.nickname || '');
  const [nicknameError, setNicknameError] = useState<string | null>(null);

  const { mutate: mutateNickname, isPending: isNicknameUpdating } = useMutation(
    {
      mutationKey: ['updateNickname'],
      mutationFn: patchNickname,
      onSuccess: () => {
        toast.success('닉네임이 변경되었습니다.');
        queryClient.invalidateQueries({ queryKey: ['members', 'me'] });
      },
      onError: () => {
        toast.error('닉네임 변경에 실패했습니다.');
      },
    },
  );

  const validateNickname = (value: string) => {
    if (value.length < 2 || value.length > 10) {
      setNicknameError('닉네임은 2자 이상 10자 이하로 입력해주세요.');
      return false;
    }
    setNicknameError(null);
    return true;
  };

  const handleNicknameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNickname(value);
    validateNickname(value);
  };

  const handleNicknameUpdate = () => {
    if (!validateNickname(nickname)) return;
    if (nickname === userInfo?.nickname) {
      toast.info('변경할 닉네임을 입력해주세요.');
      return;
    }
    mutateNickname({ nickname });
  };

  return (
    <Wrapper>
      <NicknameWrapper>
        <InputField
          name="nickname"
          label="닉네임"
          inputValue={nickname}
          onInputChange={handleNicknameChange}
          placeholder="2자 이상 10자 이하로 입력해주세요"
          errorString={nicknameError}
        />
        <Button
          text="변경"
          onClick={handleNicknameUpdate}
          disabled={isNicknameUpdating || !!nicknameError}
        />
      </NicknameWrapper>
    </Wrapper>
  );
};

export default NicknameSection;

const Wrapper = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const NicknameWrapper = styled.div`
  display: flex;
  gap: 12px;
  align-items: flex-end;
`;
