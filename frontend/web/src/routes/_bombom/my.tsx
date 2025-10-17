import { theme } from '@bombom/shared/theme';
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
import ProfileSection from '@/pages/MyPage/ProfileSection';
import SubscribedNewslettersSection from '@/pages/MyPage/SubscribedNewslettersSection';
import type { Device } from '@/hooks/useDevice';
import type { CSSObject, Theme } from '@emotion/react';
import AvatarIcon from '#/assets/svg/avatar.svg';

type MyPageTab = 'profile' | 'newsletters';

const TABS = [
  { id: 'profile', label: '내 정보' },
  { id: 'newsletters', label: '구독 뉴스레터' },
] as const;

type MyPageSearch = {
  tab: MyPageTab;
};

export const Route = createFileRoute('/_bombom/my')({
  head: () => ({
    meta: [
      {
        title: '봄봄 | 마이페이지',
      },
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
    ],
  }),
  validateSearch: (search: Record<string, unknown>): MyPageSearch => {
    const tab = search.tab as string | undefined;
    const isValidTab = (value: string): value is MyPageTab =>
      ['profile', 'newsletters'].includes(value);

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
  const activeTab = tab;

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  if (!userInfo) return null;

  const handleTabSelect = (tabId: MyPageTab) => {
    navigate({
      to: '/my',
      search: { tab: tabId },
      replace: true,
    });
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return <ProfileSection userInfo={userInfo} />;
      case 'newsletters':
        return (
          <SubscribedNewslettersSection
            newsletters={myNewsletters}
            device={device}
          />
        );
      default:
        return null;
    }
  };

  return (
    <Container device={device}>
      {device !== 'mobile' && (
        <TitleWrapper>
          <TitleIconBox>
            <AvatarIcon width={20} height={20} color={theme.colors.white} />
          </TitleIconBox>
          <Title>마이페이지</Title>
        </TitleWrapper>
      )}

      <ContentWrapper device={device}>
        <TabsWrapper device={device}>
          <Tabs direction={device === 'mobile' ? 'horizontal' : 'vertical'}>
            {TABS.map((tab) => (
              <Tab
                key={tab.id}
                value={tab.id}
                label={tab.label}
                onTabSelect={() => handleTabSelect(tab.id)}
                selected={activeTab === tab.id}
                aria-controls={`panel-${tab.id}`}
                textAlign="start"
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
  max-width: 1280px;
  margin: 0 auto;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;

  box-sizing: border-box;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
`;

const TitleIconBox = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 50%;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const ContentWrapper = styled.div<{ device: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ device }) => (device === 'pc' ? '24px' : '16px')};
  flex-direction: ${({ device }) => (device === 'pc' ? 'row' : 'column')};
  align-items: flex-start;
  align-self: stretch;
`;

const TabsWrapper = styled.div<{ device: Device }>`
  width: ${({ device }) => (device === 'pc' ? '310px' : '100%')};

  display: flex;
  flex-direction: column;

  box-sizing: border-box;

  order: ${({ device }) => (device === 'pc' ? 0 : 0)};

  ${({ device, theme }) => tabsWrapperStyles[device](theme)}
`;

const tabsWrapperStyles: Record<Device, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    flexShrink: 0,
    border: `1px solid ${theme.colors.stroke}`,
    borderRadius: '12px',
    padding: '16px',
  }),
  tablet: () => ({
    flexShrink: 0,
  }),
  mobile: () => ({
    gap: '8px',
    overflowX: 'auto',
    '&::-webkit-scrollbar': {
      display: 'none',
    },
    scrollbarWidth: 'none',
  }),
};

const TabPanel = styled.div<{ device: Device }>`
  width: 100%;
  min-width: 0;

  flex: 1;

  animation: fadein 0.2s ease-in-out;

  order: 1;

  @keyframes fadein {
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
