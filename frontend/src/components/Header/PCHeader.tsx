import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import HeaderLogo from './HeaderLogo';
import HeaderNavButtons from './HeaderNavButtons';
import HeaderProfile from './HeaderProfile';
import LoginButton from './LoginButton';
import { queries } from '@/apis/queries';
import { Nav } from '@/types/nav';

interface PCHeaderProps {
  activeNav: Nav;
}

const PCHeader = ({ activeNav }: PCHeaderProps) => {
  const { data: userInfo } = useQuery(queries.me());

  return (
    <HeaderContainer>
      <HeaderInner>
        <HeaderLogo device="pc" />

        <NavWrapper>
          <HeaderNavButtons activeNav={activeNav} device="pc" />
        </NavWrapper>

        {userInfo ? (
          <HeaderProfile userInfo={userInfo} device="pc" />
        ) : (
          <LoginButton />
        )}
      </HeaderInner>
    </HeaderContainer>
  );
};

export default PCHeader;

const HeaderContainer = styled.header`
  position: fixed;
  top: 0;
  z-index: ${({ theme }) => theme.zIndex.header};
  width: 100%;
  height: ${({ theme }) => theme.heights.headerPC};
  padding: 8px 16px;
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

const NavWrapper = styled.nav`
  padding: 4px;
  border-radius: 14px;

  display: flex;
  gap: 8px;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
`;
