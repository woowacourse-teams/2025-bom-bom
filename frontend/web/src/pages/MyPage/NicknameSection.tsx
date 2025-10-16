import styled from '@emotion/styled';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, type ChangeEvent } from 'react';
import { patchMemberInfo } from '@/apis/members';
import Button from '@/components/Button/Button';
import InputField from '@/components/InputField/InputField';
import { toast } from '@/components/Toast/utils/toastActions';
import {
  formatBirthDate,
  validateBirthDate,
} from '@/pages/signup/components/SignupCard.utils';
import type ApiError from '@/apis/ApiError';
import type { Gender } from '@/pages/signup/components/SignupCard.types';
import type { UserInfo } from '@/types/me';

interface NicknameSectionProps {
  userInfo: UserInfo;
}

const NicknameSection = ({ userInfo }: NicknameSectionProps) => {
  const queryClient = useQueryClient();

  const [nickname, setNickname] = useState(userInfo?.nickname || '');
  const [birthDate, setBirthDate] = useState(userInfo?.birthDate || '');
  const [gender, setGender] = useState<Gender>(userInfo?.gender || 'NONE');

  const [nicknameError, setNicknameError] = useState<string | null>(null);
  const [birthDateError, setBirthDateError] = useState<string | null>(null);

  const { mutate: mutateMemberInfo, isPending: isMemberInfoUpdating } =
    useMutation({
      mutationFn: patchMemberInfo,
      onSuccess: () => {
        toast.success('프로필 정보가 변경되었습니다.');
        setNicknameError(null);
        setBirthDateError(null);
        queryClient.invalidateQueries({ queryKey: ['members', 'me'] });
      },
      onError: (error: ApiError) => {
        const errorMessage =
          (error?.rawBody as { message?: string })?.message ??
          error?.message ??
          '프로필 정보 변경에 실패했습니다.';
        setNicknameError(errorMessage);
      },
    });

  const validateNickname = (value: string) => {
    if (value.length < 2 || value.length > 10) {
      setNicknameError('닉네임은 2자 이상 10자 이하로 입력해주세요.');
      return false;
    }
    setNicknameError(null);
    return true;
  };

  const handleNicknameChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNickname(value);
    validateNickname(value);
  };

  const handleBirthDateChange = (e: ChangeEvent<HTMLInputElement>) => {
    const formatted = formatBirthDate(e.target.value);
    setBirthDate(formatted);
    if (birthDateError) setBirthDateError(null);
  };

  const handleBirthDateBlur = () => {
    const error = validateBirthDate(birthDate);
    setBirthDateError(error);
  };

  const handleGenderChange = (e: ChangeEvent<HTMLInputElement>) => {
    setGender(e.target.value as Gender);
  };

  const handleProfileUpdate = () => {
    // Validate nickname
    if (!validateNickname(nickname)) return;

    // Validate birthDate if provided
    if (birthDate) {
      const birthDateValidationError = validateBirthDate(birthDate);
      if (birthDateValidationError) {
        setBirthDateError(birthDateValidationError);
        return;
      }
    }

    // Check if any field has changed
    const hasNicknameChanged = nickname !== userInfo?.nickname;
    const hasBirthDateChanged = birthDate !== (userInfo?.birthDate || '');
    const hasGenderChanged = gender !== (userInfo?.gender || 'NONE');

    if (!hasNicknameChanged && !hasBirthDateChanged && !hasGenderChanged) {
      setNicknameError('변경된 정보가 없습니다.');
      return;
    }

    mutateMemberInfo({
      nickname,
      birthDate: birthDate || undefined,
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
    </Container>
  );
};

export default NicknameSection;

const Container = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
  max-width: 400px;
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
  user-select: none;

  transition: all 0.2s ease;

  &:hover {
    border-color: ${({ theme, selected }) =>
      selected ? 'transparent' : theme.colors.primary};
  }
`;
