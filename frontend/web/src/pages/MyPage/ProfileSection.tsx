import styled from '@emotion/styled';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, type ChangeEvent } from 'react';
import Divider from '../../components/Divider/Divider';
import { postWithdraw } from '@/apis/auth';
import { patchMemberInfo } from '@/apis/members';
import Button from '@/components/Button/Button';
import InputField from '@/components/InputField/InputField';
import { toast } from '@/components/Toast/utils/toastActions';
import { useUserInfoValidation } from '@/hooks/useUserInfoValidation';
import type ApiError from '@/apis/ApiError';
import type { Gender } from '@/pages/signup/components/SignupCard.types';
import type { UserInfo } from '@/types/me';

interface ProfileSectionProps {
  userInfo: UserInfo;
}

const ProfileSection = ({ userInfo }: ProfileSectionProps) => {
  const queryClient = useQueryClient();

  const [nickname, setNickname] = useState(userInfo?.nickname || '');
  const [birthDate, setBirthDate] = useState(userInfo?.birthDate || '');
  const [gender, setGender] = useState<Gender>(userInfo?.gender || 'NONE');

  const {
    nicknameError,
    birthDateError,
    validateNicknameField,
    validateBirthDateField,
    clearNicknameError,
    clearBirthDateError,
    formatBirthDate,
    setNicknameError,
  } = useUserInfoValidation();

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

  const { mutate: mutateMemberInfo, isPending: isMemberInfoUpdating } =
    useMutation({
      mutationFn: patchMemberInfo,
      onSuccess: () => {
        toast.success('프로필 정보가 변경되었습니다.');
        clearNicknameError();
        clearBirthDateError();
        queryClient.invalidateQueries({ queryKey: ['members', 'me'] });
      },
      onError: (error: ApiError) => {
        const errorMessage =
          error?.rawBody?.message || '프로필 정보 변경에 실패했습니다.';
        setNicknameError(errorMessage);
      },
    });

  const handleNicknameChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNickname(value);
    validateNicknameField(value);
  };

  const handleBirthDateChange = (e: ChangeEvent<HTMLInputElement>) => {
    const formatted = formatBirthDate(e.target.value);
    setBirthDate(formatted);
    if (birthDateError) clearBirthDateError();
  };

  const handleBirthDateBlur = () => {
    validateBirthDateField(birthDate);
  };

  const handleGenderChange = (e: ChangeEvent<HTMLInputElement>) => {
    setGender(e.target.value as Gender);
  };

  const handleWithdrawClick = () => {
    const confirmWithdraw = confirm(
      '회원 탈퇴 시, 회원님의 모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?',
    );

    if (confirmWithdraw) {
      mutateWithdraw();
    }
  };

  const handleProfileUpdate = () => {
    if (!validateNicknameField(nickname)) return;

    if (birthDate && !validateBirthDateField(birthDate)) {
      return;
    }

    const hasNicknameChanged = nickname !== userInfo?.nickname;
    const hasBirthDateChanged = birthDate !== (userInfo?.birthDate || '');
    const hasGenderChanged = gender !== (userInfo?.gender || 'NONE');

    if (!hasNicknameChanged && !hasBirthDateChanged && !hasGenderChanged) {
      toast.error('변경된 정보가 없습니다.');
      return;
    }

    mutateMemberInfo({
      nickname,
      birthDate,
      gender,
    });
  };

  const hasError = !!nicknameError || !!birthDateError;

  return (
    <Container>
      <InputField
        name="nickname"
        label="닉네임"
        inputValue={nickname}
        onInputChange={handleNicknameChange}
        placeholder="2자 이상 10자 이하로 입력해주세요"
        errorString={nicknameError}
      />

      <FieldGroup>
        <Label>이메일</Label>
        <EmailText>{userInfo?.email}</EmailText>
      </FieldGroup>

      <InputField
        name="birthDate"
        label="생년월일(선택)"
        inputValue={birthDate}
        onInputChange={handleBirthDateChange}
        onBlur={handleBirthDateBlur}
        placeholder="YYYY-MM-DD"
        errorString={birthDateError}
      />

      <FieldGroup>
        <Label as="p">성별(선택)</Label>
        <RadioGroup role="radiogroup">
          <RadioItem>
            <HiddenRadio
              id="gender-male"
              name="gender"
              value="MALE"
              type="radio"
              checked={gender === 'MALE'}
              onChange={handleGenderChange}
            />
            <RadioButtonLabel
              selected={gender === 'MALE'}
              htmlFor="gender-male"
            >
              남성
            </RadioButtonLabel>
          </RadioItem>

          <RadioItem>
            <HiddenRadio
              id="gender-female"
              name="gender"
              value="FEMALE"
              type="radio"
              checked={gender === 'FEMALE'}
              onChange={handleGenderChange}
            />
            <RadioButtonLabel
              selected={gender === 'FEMALE'}
              htmlFor="gender-female"
            >
              여성
            </RadioButtonLabel>
          </RadioItem>

          <RadioItem>
            <HiddenRadio
              id="gender-none"
              name="gender"
              value="NONE"
              type="radio"
              checked={gender === 'NONE'}
              onChange={handleGenderChange}
            />
            <RadioButtonLabel
              selected={gender === 'NONE'}
              htmlFor="gender-none"
            >
              선택 안 함
            </RadioButtonLabel>
          </RadioItem>
        </RadioGroup>
      </FieldGroup>

      <Button
        text="변경"
        onClick={handleProfileUpdate}
        disabled={isMemberInfoUpdating || hasError}
        style={{ marginLeft: 'auto' }}
      />

      <Divider />

      <WithdrawButton type="button" onClick={handleWithdrawClick}>
        회원 탈퇴
      </WithdrawButton>
    </Container>
  );
};

export default ProfileSection;

const Container = styled.div`
  max-width: 400px;

  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const FieldGroup = styled.div`
  width: fit-content;

  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const Label = styled.label`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const EmailText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};

  user-select: text;
`;

const RadioGroup = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const RadioItem = styled.div`
  position: relative;
`;

const HiddenRadio = styled.input`
  position: absolute;
  width: 100%;
  height: 100%;
  margin: 0;

  appearance: none;
  inset: 0;
  opacity: 0;
  pointer-events: none;

  &:focus-visible + label {
    box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primaryLight};
  }
`;

const RadioButtonLabel = styled.label<{ selected: boolean }>`
  min-width: 60px;
  padding: 10px 12px;
  border: 2px solid
    ${({ theme, selected }) => (selected ? 'transparent' : theme.colors.stroke)};
  border-radius: 12px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ selected, theme }) =>
    selected ? theme.colors.primary : theme.colors.white};
  color: ${({ selected, theme }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;

  cursor: pointer;

  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    border-color: ${({ theme, selected }) =>
      selected ? 'transparent' : theme.colors.primary};
  }
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
