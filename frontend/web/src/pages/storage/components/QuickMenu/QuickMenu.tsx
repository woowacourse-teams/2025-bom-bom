import styled from '@emotion/styled';
import { useLocation, useNavigate } from '@tanstack/react-router';
import { theme } from 'shared';
import { MENU_ITEMS } from './QuickMenu.constants';
import FloatingActionButton from '@/components/FloatingActionButton/FloatingActionButton';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { useDevice } from '@/hooks/useDevice';
import type { Device } from '@/hooks/useDevice';
import LinkIcon from '#/assets/svg/link.svg';

const QuickMenu = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const device = useDevice();

  const handleTabSelect = (path: string) => {
    navigate({ to: path });
  };

  if (device !== 'pc') {
    return (
      <FloatingActionButton
        icon={<LinkIcon width={24} height={24} fill={theme.colors.white} />}
      >
        <Tabs direction="vertical">
          {MENU_ITEMS.map(({ key, label, path, Icon }) => {
            const isSelected = location.pathname.startsWith(path);

            return (
              <StyledTab
                key={key}
                value={key}
                label={label}
                selected={isSelected}
                onTabSelect={() => handleTabSelect(path)}
                StartComponent={
                  <StyledIcon as={Icon} selected={isSelected} device={device} />
                }
                textAlign="start"
                device={device}
              />
            );
          })}
        </Tabs>
      </FloatingActionButton>
    );
  }

  return (
    <Container>
      <TitleWrapper>
        <QuickMenuIconWrapper>
          <StyledLinkIcon />
        </QuickMenuIconWrapper>
        <Title>바로 가기</Title>
      </TitleWrapper>
      <Tabs direction="vertical">
        {MENU_ITEMS.map(({ key, label, path, Icon }) => {
          const isSelected = location.pathname.startsWith(path);

          return (
            <StyledTab
              key={key}
              value={key}
              label={label}
              selected={isSelected}
              onTabSelect={() => handleTabSelect(path)}
              StartComponent={
                <StyledIcon as={Icon} selected={isSelected} device={device} />
              }
              textAlign="start"
              device={device}
            />
          );
        })}
      </Tabs>
    </Container>
  );
};

export default QuickMenu;

const Container = styled.nav`
  width: 100%;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

const QuickMenuIconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const StyledLinkIcon = styled(LinkIcon)`
  width: 16px;
  height: 16px;

  color: ${({ theme }) => theme.colors.white};
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

const StyledIcon = styled.div<{ device: Device; selected?: boolean }>`
  width: ${({ device }) => (device === 'pc' ? '24px' : '28px')};
  height: ${({ device }) => (device === 'pc' ? '24px' : '28px')};

  color: ${({ theme, selected = false }) =>
    selected ? theme.colors.white : theme.colors.primary};
`;

const StyledTab = styled(Tab)<{ device: Device }>`
  font: ${({ theme, device }) =>
    device === 'pc' ? theme.fonts.body2 : theme.fonts.body1};
`;
