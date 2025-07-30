import styled from '@emotion/styled';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { useState } from 'react';
import { postSignup } from '@/apis/auth';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';

export default function SignupCard() {
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [selectedTab, setSelectedTab] = useState('MALE');

  const navigate = useNavigate();
  const { mutate: mutateSignup } = useMutation({
    mutationKey: ['signup', nickname, email, selectedTab],
    mutationFn: () =>
      postSignup({
        nickname,
        email,
        gender: selectedTab,
      }),
    onSuccess: () => {
      navigate({ to: '/login' });
    },
  });
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    mutateSignup();
  };

  return (
    <Container onSubmit={handleSubmit}>
      <EmailInput
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
        type="text"
        placeholder="닉네임을 입력해주세요."
      />
      <EmailRow>
        <EmailInput
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          type="text"
          placeholder="이메일을 입력해주세요."
        />
        @bombom.news
      </EmailRow>
      <Tabs direction="horizontal">
        <Tab
          value="MALE"
          label="남성"
          selected={selectedTab === 'MALE'}
          onTabSelect={() => setSelectedTab('MALE')}
        />
        <Tab
          value="FEMALE"
          label="여성"
          selected={selectedTab === 'FEMALE'}
          onTabSelect={() => setSelectedTab('FEMALE')}
        />
      </Tabs>
      <SubmitButton type="submit">회원가입</SubmitButton>
    </Container>
  );
}

const Container = styled.form`
  width: 410px;
  height: 540px;
  padding: 28px;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);

  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
`;

const EmailRow = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  align-items: center;
`;

const EmailInput = styled.input`
  width: 100%;
  height: 48px;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors.black};
  border-radius: 12px;
`;

const SubmitButton = styled.button`
  width: 100%;
  height: 48px;
  border-radius: 12px;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
`;
