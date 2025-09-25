import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useEffect } from 'react';
import LoginCard from '../pages/login/components/LoginCard';
import { useDevice } from '@/hooks/useDevice';
import { getResponsiveValue } from '@/utils/responsive';
import type { Device } from '@/hooks/useDevice';

export const Route = createFileRoute('/login')({
  head: () => ({
    meta: [
      {
        title: '로그인',
      },
    ],
  }),
  component: Login,
});

function Login() {
  const device = useDevice();

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has('error')) {
      const cleanUrl = window.location.pathname;
      window.history.replaceState({}, '', cleanUrl);

      alert('로그인에 실패했습니다. 다시 시도해주세요.');
    }
  }, []);

  return (
    <Container device={device}>
      <ContentWrapper device={device}>
        <LoginCard />
        <InfoText device={device}>
          {device === 'mobile'
            ? '이미 계정이 있으신가요? \n Google로 로그인하면 자동으로 연결됩니다.'
            : '이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.'}
        </InfoText>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.main<{ device: Device }>`
  min-height: 100vh;
  padding: ${({ device }) => getResponsiveValue(device, 16, 24, 32)};

  display: flex;
  align-items: center;
  justify-content: center;

  background: ${({ device, theme }) =>
    device === 'mobile'
      ? theme.colors.white
      : `linear-gradient(135deg, ${theme.colors.primary} 0%, #f74 25%, ${theme.colors.primaryLight} 100%)`};
`;

const ContentWrapper = styled.div<{ device: Device }>`
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: ${({ device }) => getResponsiveValue(device, 400, 520, 600)};

  display: flex;
  gap: ${({ device }) => getResponsiveValue(device, 20, 28, 32)};
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const InfoText = styled.p<{ device: Device }>`
  max-width: 320px;
  margin: 0;

  color: ${({ device, theme }) =>
    device === 'mobile' ? theme.colors.textSecondary : theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
  white-space: pre-line;
`;
