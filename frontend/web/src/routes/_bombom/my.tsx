import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import { queries } from '@/apis/queries';
import Tab from '@/components/Tab/Tab';
import TabItem from '@/components/Tabs/TabItem';
import Tabs from '@/components/Tabs/Tabs';
import { useDevice } from '@/hooks/useDevice';
import NicknameSection from '@/pages/MyPage/NicknameSection';
import SubscribedNewslettersSection from '@/pages/MyPage/SubscribedNewslettersSection';
import WithdrawSection from '@/pages/MyPage/WithdrawSection';
import type { Device } from '@/hooks/useDevice';

type TabId = 'profile' | 'newsletters' | 'settings';

const TABS = [
  { id: 'profile' as TabId, label: '내 정보' },
  { id: 'newsletters' as TabId, label: '구독 뉴스레터' },
  { id: 'settings' as TabId, label: '설정' },
];

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
  const [activeTab, setActiveTab] = useState<TabId>('profile');

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  if (!userInfo) return null;

  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return <NicknameSection userInfo={userInfo} />;
      case 'newsletters':
        return (
          <SubscribedNewslettersSection
            newsletters={myNewsletters}
            device={device}
          />
        );
      case 'settings':
        return <WithdrawSection />;
      default:
        return null;
    }
  };

  return (
    <Container device={device}>
      <Title device={device}>마이페이지</Title>

      <Tabs direction="vertical">
        {TABS.map((tab) => (
          <Tab
            key={tab.id}
            value={tab.id}
            label={tab.label}
            onTabSelect={() => setActiveTab(tab.id)}
            selected={activeTab === tab.id}
            aria-controls={`panel-${tab.id}`}
          />
        ))}
      </Tabs>

      <TabPanel
        id={`panel-${activeTab}`}
        role="tabpanel"
        aria-labelledby={`tab-${activeTab}`}
        device={device}
      >
        {renderTabContent()}
      </TabPanel>
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

const TabPanel = styled.div<{ device: Device }>`
  padding: ${({ device }) => (device === 'mobile' ? '16px 0' : '24px 0')};

  animation: fadeIn 0.2s ease-in-out;

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
`;
