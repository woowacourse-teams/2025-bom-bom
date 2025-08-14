import styled from '@emotion/styled';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { FormEvent, useState } from 'react';
import { postSignup } from '@/apis/auth';
import InputField from '@/components/InputField/InputField';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { theme } from '@/styles/theme';
import HelpIcon from '#/assets/help.svg';

type Gender = 'MALE' | 'FEMALE';

const SignupCard = () => {
  const [nickname, setNickname] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [emailPart, setEmailPart] = useState('');
  const [selectedGender, setSelectedGender] = useState<Gender>('MALE');
  const [emailHelpOpen, setEmailHelpOpen] = useState(false);

  const navigate = useNavigate();

  const email = `${emailPart.trim()}@bombom.news`;

  const { mutate: mutateSignup, isPending } = useMutation({
    mutationKey: ['signup', nickname, email, selectedGender],
    mutationFn: () =>
      postSignup({
        nickname: nickname.trim(),
        email,
        gender: selectedGender,
      }),
    onSuccess: () => {
      navigate({ to: '/login' });
    },
  });

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    mutateSignup();
  };

  const openWhy = () => setEmailHelpOpen(true);
  const closeWhy = () => setEmailHelpOpen(false);

  return (
    <Container>
      <SignupForm onSubmit={handleSubmit}>
        <HeaderWrapper>
          <Title>회원가입</Title>
          <Description>봄봄과 함께 오늘의 뉴스레터를 시작해요.</Description>
        </HeaderWrapper>

        <InputField
          name="닉네임"
          label="닉네임"
          inputValue={nickname}
          onInputChange={(e) => setNickname(e.target.value)}
          placeholder="닉네임을 입력해주세요"
        />

        <InputField
          name="생년월일"
          label="생년월일"
          inputValue={birthDate}
          onInputChange={(e) => setBirthDate(e.target.value)}
          placeholder="생년월일 입력해주세요"
        />

        <InputField
          name="email"
          label={
            <LabelRow>
              <Label htmlFor="email">이메일</Label>
              <InfoButton
                type="button"
                aria-label="이메일을 수집하는 이유 안내"
                aria-expanded={emailHelpOpen}
                aria-describedby="email-why-tooltip"
                onMouseEnter={openWhy}
                onMouseLeave={closeWhy}
                onFocus={openWhy}
                onBlur={closeWhy}
              >
                <EmailHelpIcon fill={theme.colors.primary} />
              </InfoButton>

              <Tooltip
                role="tooltip"
                id="email-why-tooltip"
                data-open={emailHelpOpen}
              >
                봄봄은 <b>개인 메일</b>이 아닌 <b>봄봄 전용 메일</b>(
                <b>@bombom.news</b>)로 뉴스레터를 <b>구독</b>해요. 여기서 만드는
                주소는 봄봄 서비스에서 발행·관리되며, 구독/알림에 사용됩니다.
              </Tooltip>
            </LabelRow>
          }
          inputValue={emailPart}
          onInputChange={(e) => setEmailPart(e.target.value)}
          placeholder="이메일을 입력해주세요"
          suffix={<Suffix>@bombom.news</Suffix>}
        />

        <FieldGroup>
          <Label as="div">성별</Label>
          <Tabs direction="horizontal">
            <Tab
              value="MALE"
              label="남성"
              selected={selectedGender === 'MALE'}
              onTabSelect={() => setSelectedGender('MALE')}
            />
            <Tab
              value="FEMALE"
              label="여성"
              selected={selectedGender === 'FEMALE'}
              onTabSelect={() => setSelectedGender('FEMALE')}
            />
          </Tabs>
        </FieldGroup>

        <SubmitButton type="submit">
          {isPending ? '가입 중…' : '회원가입'}
        </SubmitButton>
      </SignupForm>
    </Container>
  );
};

export default SignupCard;

const Container = styled.div`
  width: min(100%, 420px);
  padding: 28px 24px;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);

  background-color: ${({ theme }) => theme.colors.white};
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
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const Label = styled.label`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
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

const EmailHelpIcon = styled(HelpIcon)`
  background-color: ${({ theme }) => theme.colors.white};
`;

const Tooltip = styled.div`
  visibility: hidden;
  position: absolute;
  bottom: 28px;
  left: 0;
  z-index: 10;
  width: 100%;
  max-width: 260px;
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

  &[data-open='true'] {
    visibility: visible;

    opacity: 1;
    transform: translateY(0);
  }
`;

const Suffix = styled.span`
  height: 100%;
  padding: 0 10px;
  border-radius: 10px;

  display: inline-flex;
  align-items: center;

  background: ${({ theme }) => theme.colors.disabledBackground};
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  user-select: none;
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
