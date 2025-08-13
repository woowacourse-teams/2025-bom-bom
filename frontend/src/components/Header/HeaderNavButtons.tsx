import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import HomeIcon from '../../../public/assets/home.svg';
import { DeviceType } from '@/hooks/useDeviceType';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { NavType } from '@/types/nav';
import CompassIcon from '#/assets/compass.svg';
import StorageIcon from '#/assets/storage.svg';

interface HeaderNavButtonsProps {
  activeNav: NavType;
  deviceType: DeviceType;
}

const HeaderNavButtons = ({ activeNav, deviceType }: HeaderNavButtonsProps) => {
  return (
    <>
      <NavButton
        active={activeNav === 'today'}
        deviceType={deviceType}
        to="/"
        onClick={() => {
          trackEvent({
            category: 'Navigation',
            action: 'Click Today Nav',
            label: 'Go to Today',
          });
        }}
      >
        <HomeIcon
          width={24}
          height={24}
          color={activeNav === 'today' ? 'white' : 'black'}
        />
        <p>오늘의 뉴스레터</p>
      </NavButton>
      <NavButton
        active={activeNav === 'storage'}
        deviceType={deviceType}
        to="/storage"
        onClick={() => {
          trackEvent({
            category: 'Navigation',
            action: 'Click Storage Nav',
            label: 'Go to Storage',
          });
        }}
      >
        <StorageIcon
          width={24}
          height={24}
          color={activeNav === 'storage' ? 'white' : 'black'}
        />
        <p>뉴스레터 보관함</p>
      </NavButton>
      <NavButton
        active={activeNav === 'recommend'}
        deviceType={deviceType}
        to="/recommend"
        onClick={() => {
          trackEvent({
            category: 'Navigation',
            action: 'Click Recommend Nav',
            label: 'Go to Recommend',
          });
        }}
      >
        <CompassIcon
          width={24}
          height={24}
          color={activeNav === 'recommend' ? 'white' : 'black'}
        />
        <p>뉴스레터 추천</p>
      </NavButton>
    </>
  );
};

export default HeaderNavButtons;

const NavButton = styled(Link)<{ active?: boolean; deviceType: DeviceType }>`
  padding: ${({ deviceType }) =>
    deviceType === 'mobile' ? '4px 12px' : '10px 12px'};
  border-radius: 12px;

  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'mobile' ? '0px' : '4px')};
  flex-direction: ${({ deviceType }) =>
    deviceType === 'mobile' ? 'column' : 'row'};
  align-items: center;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};
  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  font: ${({ theme }) => theme.fonts.body2};
`;
