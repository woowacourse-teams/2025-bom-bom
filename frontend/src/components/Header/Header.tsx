import styled from '@emotion/styled';
import HeaderLogo from './HeaderLogo';
import HeaderNavButtons from './HeaderNavButtons';
import HeaderProfile from './HeaderProfile';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { NavType } from '@/types/nav';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav }: HeaderProps) {
  const deviceType = useDeviceType();

  return deviceType === 'mobile' ? (
    <>
      <MobileHeaderContainer>
        <HeaderLogo deviceType={deviceType} />
        <HeaderProfile />
      </MobileHeaderContainer>

      <BottomNavWrapper>
        <HeaderNavButtons activeNav={activeNav} deviceType={deviceType} />
      </BottomNavWrapper>
    </>
  ) : (
    <HeaderContainer>
      <HeaderInner>
        <HeaderLogo deviceType={deviceType} />

        <NavWrapper deviceType={deviceType}>
          <HeaderNavButtons activeNav={activeNav} deviceType={deviceType} />
        </NavWrapper>

        <HeaderProfile />
      </HeaderInner>
    </HeaderContainer>
  );
}

const MobileHeaderContainer = styled.header`
  position: fixed;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 56px;
  padding: 8px 12px;
  box-shadow:
    0 8px 12px -6px rgb(0 0 0 / 10%),
    0 3px 5px -4px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: space-between;

  background: ${({ theme }) => theme.colors.white};
`;

const HeaderContainer = styled.header`
  position: fixed;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 72px;
  padding: 8px 16px;
  border-radius: 0 0 8px 8px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.white};
`;

const HeaderInner = styled.div`
  width: 100%;
  max-width: 1280px;

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const NavWrapper = styled.nav<{ deviceType: DeviceType }>`
  padding: 4px;
  border-radius: 14px;

  display: flex;
  gap: ${({ deviceType }) => (deviceType !== 'pc' ? '4px' : '8px')};
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
`;

const BottomNavWrapper = styled.nav`
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  height: 64px;
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  border-top: 1px solid ${({ theme }) => theme.colors.stroke};
  box-shadow: 0 -8px 12px -6px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: space-around;

  background: ${({ theme }) => theme.colors.white};
`;
