import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import HeaderLogo from './HeaderLogo';
import HeaderNavButtons from './HeaderNavButtons';
import HeaderProfile from './HeaderProfile';
import LoginButton from './LoginButton';
import { queries } from '@/apis/queries';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { NavType } from '@/types/nav';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav }: HeaderProps) {
  const { data: userInfo, isFetching } = useQuery(queries.me());

  const deviceType = useDeviceType();

  const isLoggedIn = isFetching || userInfo;

  return (
    <>
      {deviceType === 'mobile' ? (
        <>
          <MobileHeaderContainer>
            <HeaderLogo deviceType={deviceType} />
            {isLoggedIn && userInfo ? (
              <HeaderProfile userInfo={userInfo} deviceType={deviceType} />
            ) : (
              <LoginButton />
            )}
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

            {isLoggedIn && userInfo ? (
              <HeaderProfile userInfo={userInfo} deviceType={deviceType} />
            ) : (
              <LoginButton />
            )}
          </HeaderInner>
        </HeaderContainer>
      )}
    </>
  );
}

const MobileHeaderContainer = styled.header`
  position: fixed;
  top: 0;
  z-index: ${({ theme }) => theme.zIndex.header};
  width: 100%;
  height: calc(
    ${({ theme }) => theme.heights.headerMobile} + env(safe-area-inset-top)
  );
  padding: 8px 12px;
  padding-top: calc(8px + env(safe-area-inset-top));
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
  z-index: ${({ theme }) => theme.zIndex.header};
  width: 100%;
  height: ${({ theme }) => theme.heights.headerPC};
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
  z-index: ${({ theme }) => theme.zIndex.header};
  height: calc(
    ${({ theme }) => theme.heights.bottomNav} + env(safe-area-inset-bottom)
  );
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  border-top: 1px solid ${({ theme }) => theme.colors.stroke};
  box-shadow: 0 -8px 12px -6px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: space-around;

  background: ${({ theme }) => theme.colors.white};
`;
