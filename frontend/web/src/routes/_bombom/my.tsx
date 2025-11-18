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
import NotificationSettingsSection from '@/pages/MyPage/NotificationSettingsSection';
import ProfileSection from '@/pages/MyPage/ProfileSection';
import SubscribedNewslettersSection from '@/pages/MyPage/SubscribedNewslettersSection';
import { isWebView } from '@/utils/device';
import type { Device } from '@/hooks/useDevice';
import type { CSSObject, Theme } from '@emotion/react';
import AvatarIcon from '#/assets/svg/avatar.svg';

type MyPageTab = 'profile' | 'newsletters' | 'notification';

const DEFAULT_TABS = [
  { id: 'profile', label: '내 정보' },
  { id: 'newsletters', label: '구독 뉴스레터' },
] as const;

const WEBVIEW_TABS = [{ id: 'notification', label: '알림 설정' }] as const;

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
  validateSearch: (search: { tab?: MyPageTab }) => {
    return { tab: search.tab ?? 'profile' };
  },
  component: MyPage,
});

function MyPage() {
  const device = useDevice();
  const navigate = useNavigate();
  const { tab: activeTabParam } = useSearch({ from: '/_bombom/my' });

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  const tabs = isWebView() ? [...DEFAULT_TABS, ...WEBVIEW_TABS] : DEFAULT_TABS;

  if (!userInfo) return null;

  const handleTabSelect = (tabId: MyPageTab) => {
    navigate({
      to: '/my',
      search: { tab: tabId },
      replace: true,
    });
  };

  const renderTabContent = () => {
    switch (activeTabParam) {
      case 'profile':
        return <ProfileSection userInfo={userInfo} />;
      case 'newsletters':
        return (
          <SubscribedNewslettersSection
            newsletters={myNewsletters ?? []}
            device={device}
          />
        );
      case 'notification':
        return <NotificationSettingsSection />;
      default:
        return null;
    }
  };

  return (
    <Container>
      <TitleWrapper>
        <TitleIconBox>
          <AvatarIcon width={20} height={20} color={theme.colors.white} />
        </TitleIconBox>
        <Title>마이페이지</Title>
      </TitleWrapper>

      <ContentWrapper device={device}>
        <TabsWrapper device={device}>
          <Tabs direction={device === 'mobile' ? 'horizontal' : 'vertical'}>
            {tabs.map((tab) => (
              <Tab
                key={tab.id}
                value={tab.id}
                label={tab.label}
                onTabSelect={() => handleTabSelect(tab.id)}
                selected={activeTabParam === tab.id}
                aria-controls={`panel-${tab.id}`}
                textAlign="start"
              />
            ))}
          </Tabs>
        </TabsWrapper>

        <TabPanel
          id={`panel-${activeTabParam}`}
          role="tabpanel"
          aria-labelledby={`tab-${activeTabParam}`}
          device={device}
        >
          {renderTabContent()}
        </TabPanel>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
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
  gap: ${({ device }) => (device === 'mobile' ? '16px' : '20px')};
  flex-direction: ${({ device }) => (device === 'mobile' ? 'column' : 'row')};
  align-items: flex-start;
  align-self: stretch;
`;

const TabsWrapper = styled.div<{ device: Device }>`
  width: ${({ device }) => (device === 'mobile' ? '100%' : '280px')};

  display: flex;
  flex-direction: column;

  box-sizing: border-box;

  order: 0;

  ${({ device, theme }) => tabsWrapperStyles[device](theme)}
`;

const tabsWrapperStyles: Record<Device, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    flexShrink: 0,
    border: `1px solid ${theme.colors.stroke}`,
    borderRadius: '12px',
    padding: '16px',
  }),
  tablet: (theme) => ({
    flexShrink: 0,
    border: `1px solid ${theme.colors.stroke}`,
    borderRadius: '12px',
    padding: '16px',
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
