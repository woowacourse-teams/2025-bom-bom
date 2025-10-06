import styled from '@emotion/styled';
import HeaderLogo from './HeaderLogo';
import HeaderNavButtons from './HeaderNavButtons';
import HeaderProfile from './HeaderProfile';
import LoginButton from './LoginButton';
import Skeleton from '../Skeleton/Skeleton';
import { useUserInfo } from '@/hooks/useUserInfo';
import type { Nav } from '@/types/nav';

interface MobileHeaderProps {
  activeNav: Nav;
}

const MobileHeader = ({ activeNav }: MobileHeaderProps) => {
  const { userInfo, isLoading } = useUserInfo();

  return (
    <>
      <MobileHeaderContainer>
        <HeaderLogo device="mobile" />
        {isLoading ? (
          <Skeleton width="120px" height="40px" />
        ) : userInfo ? (
          <HeaderProfile userInfo={userInfo} device="mobile" />
        ) : (
          <LoginButton />
        )}
      </MobileHeaderContainer>
      <BottomNavWrapper>
        <HeaderNavButtons activeNav={activeNav} device="mobile" />
      </BottomNavWrapper>
    </>
  );
};

export default MobileHeader;

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
