import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import {
  createFileRoute,
  useNavigate,
  useSearch,
} from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import Tab from '@/components/Tab/Tab';
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

type MyPageSearch = {
  tab?: TabId;
};

export const Route = createFileRoute('/_bombom/my')({
  head: () => ({
    meta: [
      {
        title: '봄봄 | 마이페이지',
      },
    ],
  }),
  validateSearch: (search: Record<string, unknown>): MyPageSearch => {
    const tab = search.tab as string | undefined;
    const isValidTab = (value: string): value is TabId =>
      ['profile', 'newsletters', 'settings'].includes(value);

    return {
      tab: tab && isValidTab(tab) ? tab : 'profile',
    };
  },
  component: MyPage,
});

function MyPage() {
  const device = useDevice();
  const navigate = useNavigate();
  const { tab } = useSearch({ from: '/_bombom/my' });
  const activeTab = tab || 'profile';

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  if (!userInfo) return null;

  const handleTabSelect = (tabId: TabId) => {
    navigate({
      to: '/my',
      search: { tab: tabId },
      replace: true,
    });
  };

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

      <ContentWrapper device={device}>
        <TabsWrapper device={device}>
          <Tabs direction="vertical">
            {TABS.map((tab) => (
              <Tab
                key={tab.id}
                value={tab.id}
                label={tab.label}
                onTabSelect={() => handleTabSelect(tab.id)}
                selected={activeTab === tab.id}
                aria-controls={`panel-${tab.id}`}
              />
            ))}
          </Tabs>
        </TabsWrapper>

        <TabPanel
          id={`panel-${activeTab}`}
          role="tabpanel"
          aria-labelledby={`tab-${activeTab}`}
          device={device}
        >
          {renderTabContent()}
        </TabPanel>
      </ContentWrapper>
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

const ContentWrapper = styled.div<{ device: Device }>`
  display: flex;
  gap: 24px;
  flex-direction: ${({ device }) => (device === 'mobile' ? 'column' : 'row')};
  align-items: ${({ device }) =>
    device === 'mobile' ? 'stretch' : 'flex-start'};
`;

const TabsWrapper = styled.div<{ device: Device }>`
  flex-shrink: 0;
  width: ${({ device }) => (device === 'mobile' ? '100%' : '200px')};
`;

const TabPanel = styled.div<{ device: Device }>`
  flex: 1;
  min-width: 0;

  animation: fadeIn 0.2s ease-in-out;

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateX(-8px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }
`;
