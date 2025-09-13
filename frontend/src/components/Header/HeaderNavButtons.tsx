import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import HomeIcon from '../../../public/assets/home.svg';
import { DeviceType } from '@/hooks/useDeviceType';
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
      <NavButton active={activeNav === 'today'} deviceType={deviceType} to="/">
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
    active ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ deviceType, theme }) =>
    deviceType === 'mobile' ? theme.fonts.body3 : theme.fonts.body2};

  text-shadow: ${({ active }) =>
    active ? 'none' : '0 1px 2px rgba(0, 0, 0, 0.1)'};
  transition: all 0.2s ease;

  &:hover {
    background: ${({ active, theme }) =>
      active ? theme.colors.primary : theme.colors.dividers};
    transform: translateY(-1px);
  }
`;
