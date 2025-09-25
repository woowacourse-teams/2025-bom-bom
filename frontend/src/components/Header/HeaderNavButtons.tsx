import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import type { Device } from '@/hooks/useDevice';
import type { Nav } from '@/types/nav';
import CompassIcon from '#/assets/svg/compass.svg';
import HomeIcon from '#/assets/svg/home.svg';
import StorageIcon from '#/assets/svg/storage.svg';

interface HeaderNavButtonsProps {
  activeNav: Nav;
  device: Device;
}

const HeaderNavButtons = ({ activeNav, device }: HeaderNavButtonsProps) => {
  return (
    <>
      <NavButton active={activeNav === 'today'} device={device} to="/today">
        <HomeIcon
          width={24}
          height={24}
          color={activeNav === 'today' ? 'white' : 'black'}
        />
        <p>오늘의 뉴스레터</p>
      </NavButton>
      <NavButton active={activeNav === 'storage'} device={device} to="/storage">
        <StorageIcon
          width={24}
          height={24}
          color={activeNav === 'storage' ? 'white' : 'black'}
        />
        <p>뉴스레터 보관함</p>
      </NavButton>
      <NavButton active={activeNav === 'recommend'} device={device} to="/">
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

const NavButton = styled(Link, {
  shouldForwardProp: (prop) => prop !== 'active' && prop !== 'device',
})<{ active?: boolean; device: Device }>`
  padding: ${({ device }) => (device === 'mobile' ? '4px 12px' : '10px 12px')};
  border-radius: 12px;

  display: flex;
  gap: ${({ device }) => (device === 'mobile' ? '0px' : '4px')};
  flex-direction: ${({ device }) => (device === 'mobile' ? 'column' : 'row')};
  align-items: center;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};
  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ device, theme }) =>
    device === 'mobile' ? theme.fonts.body3 : theme.fonts.body2};

  text-shadow: ${({ active }) =>
    active ? 'none' : '0 1px 2px rgba(0, 0, 0, 0.1)'};
  transition: all 0.2s ease;

  &:hover {
    background: ${({ active, theme }) =>
      active ? theme.colors.primary : theme.colors.dividers};
    transform: translateY(-1px);
  }
`;
