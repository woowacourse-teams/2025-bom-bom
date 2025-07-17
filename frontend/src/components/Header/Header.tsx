import styled from '@emotion/styled';
import compassIcon from '../../../public/assets/compass.svg';
import homeIcon from '../../../public/assets/home.svg';
import copyIcon from '../../../public/assets/copy.svg';

type NavType = 'home' | 'recommend';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav = 'home' }: HeaderProps) {
  const handleCopyEmail = () => {
    navigator.clipboard.writeText('test@bombom.news');
  };

  return (
    <HeaderContainer>
      <HeaderInner>
        <LogoWrapper>
          <LogoBox>
            <LogoImg src={homeIcon} alt="logo" />
          </LogoBox>
          <TitleBox>
            <Title>봄봄</Title>
            <SubTitle>당신의 하루에 찾아오는 작은 설렘</SubTitle>
          </TitleBox>
        </LogoWrapper>
        <Nav>
          <NavButton active={activeNav === 'home'}>
            <NavIcon src={homeIcon} alt="home" />
            <p>오늘의 뉴스레터</p>
          </NavButton>
          <NavButton active={activeNav === 'recommend'}>
            <NavIcon src={compassIcon} alt="compass" />
            <p>뉴스레터 추천</p>
          </NavButton>
        </Nav>
        <ProfileBox>
          <ProfileImg src={compassIcon} alt="profile" />
          <ProfileInfo>
            <ProfileName>김봄봄</ProfileName>
            <ProfileEmail onClick={handleCopyEmail}>
              <EmailText>test@bombom.news</EmailText>
              <CopyIcon src={copyIcon} alt="copy" />
            </ProfileEmail>
          </ProfileInfo>
        </ProfileBox>
      </HeaderInner>
    </HeaderContainer>
  );
}

const HeaderContainer = styled.header`
  background: ${({ theme }) => theme.colors.white};
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow:
    0px 10px 15px -3px rgba(0, 0, 0, 0.1),
    0px 4px 6px -4px rgba(0, 0, 0, 0.1);
  border-radius: 0 0 8px 8px;
  padding: 8px 16px;
  height: 64px;
`;

const HeaderInner = styled.div`
  width: 100%;
  max-width: 1280px;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const LogoWrapper = styled.div`
  display: flex;
  align-items: center;
`;

const LogoBox = styled.div`
  width: 40px;
  height: 40px;
  background: ${({ theme }) => theme.colors.primary};
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0px 10px 15px -3px rgba(0, 0, 0, 0.1),
    0px 4px 6px -4px rgba(0, 0, 0, 0.1);
  margin-right: 12px;
`;

const LogoImg = styled.img`
  width: 20px;
  height: 20px;
`;

const TitleBox = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.heading4};
  background: linear-gradient(
    144.324deg,
    #4b5563 0%,
    #ff9966 50%,
    #ffd700 100%
  );
  background-clip: text;
  -webkit-text-fill-color: transparent;
`;

const SubTitle = styled.div`
  ${({ theme }) => theme.fonts.body2};
`;

const Nav = styled.nav`
  display: flex;
  gap: 8px;
  align-items: center;
  background: ${({ theme }) => theme.colors.white};
  border-radius: 14px;
  padding: 4px;
`;

const NavButton = styled.button<{ active?: boolean }>`
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 12px;
  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};
  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  ${({ theme }) => theme.fonts.body2};
`;

const NavIcon = styled.img`
  width: 20px;
  height: 20px;
`;

const CopyIcon = styled.img`
  width: 16px;
  height: 16px;
`;

const ProfileBox = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 12px;
  background: ${({ theme }) => theme.colors.white};
`;

const ProfileImg = styled.img`
  width: 28px;
  height: 28px;
  border-radius: 50%;
`;

const ProfileInfo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
`;

const ProfileName = styled.div`
  ${({ theme }) => theme.fonts.caption};
`;

const ProfileEmail = styled.div`
  ${({ theme }) => theme.fonts.caption};
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 4px;
`;

const EmailText = styled.div`
  ${({ theme }) => theme.fonts.caption};
`;
