import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import { useDevice } from '@/hooks/useDevice';
import NicknameSection from '@/pages/MyPage/NicknameSection';
import SubscribedNewslettersSection from '@/pages/MyPage/SubscribedNewslettersSection';
import WithdrawSection from '@/pages/MyPage/WithdrawSection';
import type { Device } from '@/hooks/useDevice';

export const Route = createFileRoute('/_bombom/my')({
  head: () => ({
    meta: [
      {
        title: '봄봄 | 마이페이지',
      },
    ],
  }),
  component: MyPage,
});

function MyPage() {
  const device = useDevice();

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  if (!userInfo) return null;

  return (
    <Container device={device}>
      <Title device={device}>마이페이지</Title>

      <NicknameSection userInfo={userInfo} device={device} />

      <Divider />

      <SubscribedNewslettersSection
        newsletters={myNewsletters}
        device={device}
      />

      <Divider />

      <WithdrawSection device={device} />
    </Container>
  );
}

const Container = styled.div<{ device: Device }>`
  width: 100%;
  max-width: ${({ device }) => (device === 'mobile' ? '100%' : '800px')};
  margin: 0 auto;
  padding: ${({ device }) =>
    device === 'mobile' ? '16px' : device === 'tablet' ? '24px' : '32px'};

  display: flex;
  gap: 24px;
  flex-direction: column;
`;

const Title = styled.h1<{ device: Device }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, device }) =>
    device === 'mobile' ? theme.fonts.heading3 : theme.fonts.heading2};
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;
  margin: 8px 0;

  background-color: ${({ theme }) => theme.colors.dividers};
`;
