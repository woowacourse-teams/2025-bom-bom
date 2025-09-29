import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { useLocation } from '@tanstack/react-router';
import { useEffect, useState } from 'react';
import { formatBirthDate, validateBirthDate } from './SignupCard.utils';
import { useSignupMutation } from '../hooks/useSignupMutation';
import InputField from '@/components/InputField/InputField';
import { useDevice } from '@/hooks/useDevice';
import { GUIDE_MAILS } from '@/mocks/datas/guideMail';
import { theme } from '@/styles/theme';
import { formatDate } from '@/utils/date';
import { createStorage } from '@/utils/localStorage';
import type { FieldError, Gender } from './SignupCard.types';
import type { Device } from '@/hooks/useDevice';
import type { ChangeEvent, FormEvent } from 'react';
import HelpIcon from '#/assets/svg/help.svg';

const EMAIL_DOMAIN = '@bombom.news';

const SignupCard = () => {
  const location = useLocation();
  const device = useDevice();

  const [nickname, setNickname] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [emailPart, setEmailPart] = useState('');
  const [gender, setGender] = useState<Gender>('NONE');
  const [emailHelpOpen, setEmailHelpOpen] = useState(false);

  const [birthDateError, setBirthDateError] = useState<FieldError>(null);

  const email = `${emailPart.trim()}${EMAIL_DOMAIN}`;
  const isFormValid = nickname && emailPart && (!birthDate || !birthDateError);

  const { mutate: signup } = useSignupMutation({
    nickname,
    email,
    gender,
    birthDate,
  });

  const handleBirthDateBlur = () => {
    setBirthDateError(validateBirthDate(birthDate));
  };

  const handleBirthDateChange = (e: ChangeEvent<HTMLInputElement>) => {
    const target = e.currentTarget;
    const formatted = formatBirthDate(target.value);
    setBirthDate(formatted);
    if (birthDateError) setBirthDateError(null);
  };

  const handleGenderChange = (e: ChangeEvent<HTMLInputElement>) => {
    setGender(e.target.value as Gender);
  };

  const addGuideMail = () => {
    const guideMail = GUIDE_MAILS.map((mail) => ({
      ...mail,
      createdAt: formatDate(new Date()),
    }));
    createStorage('guide-mail').set(guideMail);
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    signup();
    addGuideMail();
  };

  const openEmailHelp = () => setEmailHelpOpen(true);
  const closeEmailHelp = () => setEmailHelpOpen(false);

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);

    const emailParam = searchParams.get('email');
    const nameParam = searchParams.get('name');

    if (emailParam) {
      setEmailPart(emailParam);
    }

    if (nameParam) {
      setNickname(nameParam);
    }

    if (emailParam || nameParam) {
      const cleanUrl = window.location.pathname;
      window.history.replaceState({}, '', cleanUrl);
    }
  }, [location.search]);

  return (
    <Container device={device}>
      <SignupForm onSubmit={handleSubmit}>
        <HeaderWrapper>
          <Title>회원가입</Title>
          <Description>봄봄과 함께 오늘의 뉴스레터를 시작해요.</Description>
        </HeaderWrapper>

        <FieldGroup>
          <Label>닉네임</Label>
          <TextValue>{nickname}</TextValue>
        </FieldGroup>

        <FieldGroup>
          <LabelRow>
            <Label htmlFor="email">이메일</Label>
            <InfoButton
              type="button"
              aria-label="이메일을 수집하는 이유 안내"
              aria-expanded={emailHelpOpen}
              aria-describedby="email-why-tooltip"
              onMouseEnter={openEmailHelp}
              onMouseLeave={closeEmailHelp}
              onFocus={openEmailHelp}
              onBlur={closeEmailHelp}
            >
              <EmailHelpIcon fill={theme.colors.primary} />
            </InfoButton>
            <InfoText>이 주소로 뉴스레터가 도착해요!</InfoText>

            <Tooltip
              role="tooltip"
              id="email-help-tooltip"
              open={emailHelpOpen}
            >
              봄봄은 <b>개인 메일</b>이 아닌 <b>봄봄 전용 메일</b>(
              <b>{EMAIL_DOMAIN}</b>)로 뉴스레터를 <b>수신</b>해요.
              <br />- 뉴스레터 전용이라 깔끔하게 관리돼요.
              <br />- 구독/알림/차단 같은 관리 기능에 이 주소를 사용해요.
              <br />- 일반 메일 송수신은 지원하지 않아요. (수신 전용)
            </Tooltip>
          </LabelRow>
          <TextValue>{emailPart + EMAIL_DOMAIN}</TextValue>
        </FieldGroup>

        <InputField
          name="생년월일(선택)"
          label="생년월일(선택)"
          inputValue={birthDate}
          onInputChange={handleBirthDateChange}
          onBlur={handleBirthDateBlur}
          errorString={birthDateError}
          placeholder="YYYY-MM-DD"
        />

        <FieldGroup>
          <Label as="p">성별(선택)</Label>
          <RadioGroup role="radiogroup" aria-describedby="gender-hint">
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
                onChange={(e) => setGender(e.target.value as Gender)}
              />
              <RadioButtonLabel
                selected={gender === 'FEMALE'}
                htmlFor="gender-female"
              >
                여성
              </RadioButtonLabel>
            </RadioItem>
          </RadioGroup>
        </FieldGroup>

        <SubmitButton type="submit" disabled={!isFormValid}>
          회원가입
        </SubmitButton>
      </SignupForm>
    </Container>
  );
};

export default SignupCard;

const Container = styled.div<{ device: Device }>`
  width: min(100%, 420px);
  padding: 28px 24px;

  background-color: ${({ theme }) => theme.colors.white};

  ${({ device }) =>
    device !== 'mobile' &&
    `
    border-radius: 20px;
    box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);
  `}
`;

const SignupForm = styled.form`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const HeaderWrapper = styled.div`
  display: flex;
  gap: 6px;
  flex-direction: column;
`;

const Title = styled.h4`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
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

const TextValue = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};

  user-select: text;
`;

const LabelRow = styled.div`
  position: relative;

  display: inline-flex;
  gap: 6px;
  align-items: center;
`;

const InfoButton = styled.button`
  width: 18px;
  height: 18px;
  padding: 0;
  border: none;
  border-radius: 50%;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.disabledBackground};
  color: ${({ theme }) => theme.colors.textSecondary};

  cursor: help;

  &:focus {
    outline: none;
    box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primaryLight};
  }
`;

const InfoText = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const EmailHelpIcon = styled(HelpIcon)`
  background-color: ${({ theme }) => theme.colors.white};
`;

const Tooltip = styled.div<{ open: boolean }>`
  visibility: hidden;
  position: absolute;
  bottom: 28px;
  left: 0;
  z-index: ${({ theme }) => theme.zIndex.elevated};
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  box-shadow: 0 10px 20px -12px rgb(0 0 0 / 35%);

  background: ${({ theme }) => theme.colors.black};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.caption};

  opacity: 0;
  transform: translateY(4px);
  transition:
    opacity 0.15s ease,
    transform 0.15s ease,
    visibility 0.15s;

  ${({ open }) =>
    open &&
    css`
      visibility: visible;

      opacity: 1;
      transform: translateY(0);
    `}
`;

const SubmitButton = styled.button`
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading6};

  transition:
    transform 0.06s ease,
    box-shadow 0.2s ease,
    filter 0.2s ease;

  &:hover {
    box-shadow: 0 10px 20px -10px rgb(0 0 0 / 25%);
    filter: brightness(1.02);
  }

  &:active {
    transform: translateY(1px);
  }

  &:disabled {
    box-shadow: none;

    background: ${({ theme }) => theme.colors.disabledBackground};
    color: ${({ theme }) => theme.colors.disabledText};

    cursor: not-allowed;
  }
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
  font-weight: ${({ selected }) => (selected ? '600' : '400')};
  text-align: center;

  user-select: none;
`;
