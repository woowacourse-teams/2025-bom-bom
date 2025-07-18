import styled from '@emotion/styled';
import compassIcon from '../../../public/assets/compass.svg';
import copyIcon from '../../../public/assets/copy.svg';
import CompassIcon from '../icons/CompassIcon';
import HomeIcon from '../icons/HomeIcon';

type NavType = 'home' | 'recommend';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav }: HeaderProps) {
  const handleCopyEmail = () => {
    navigator.clipboard.writeText('test@bombom.news');
  };

  return (
    <>
      <HeaderContainer>
        <HeaderInner>
          <LogoWrapper>
            <LogoBox>
              <HomeIcon />
            </LogoBox>
            <TitleBox>
              <Title>봄봄</Title>
              <SubTitle>당신의 하루에 찾아오는 작은 설렘</SubTitle>
            </TitleBox>
          </LogoWrapper>

          <Nav>
            <NavButton active={activeNav === 'home'}>
              <HomeIcon color={activeNav === 'home' ? 'white' : 'black'} />
              <p>오늘의 뉴스레터</p>
            </NavButton>
            <NavButton active={activeNav === 'recommend'}>
              <CompassIcon
                color={activeNav === 'recommend' ? 'white' : 'black'}
              />
              <p>뉴스레터 추천</p>
            </NavButton>
          </Nav>

          <ProfileBox>
            <ProfileImg src={compassIcon} alt="profile" />
            <ProfileInfo>
              <ProfileName>김봄봄</ProfileName>
              <ProfileEmail onClick={handleCopyEmail}>
                <EmailText>test@bombom.news</EmailText>
                <img src={copyIcon} alt="copy" width={16} height={16} />
              </ProfileEmail>
            </ProfileInfo>
          </ProfileBox>
        </HeaderInner>
      </HeaderContainer>
      <EmptyContainer />
    </>
  );
}

// 헤더의 공간을 차지하기 위한 컴포넌트
const EmptyContainer = styled.div`
  visibility: hidden;
  height: 64px;
`;

const HeaderContainer = styled.header`
  position: fixed;
  top: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 100%;
  height: 64px;
  padding: 8px 16px;
  border-radius: 0 0 8px 8px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  background: ${({ theme }) => theme.colors.white};
`;

const HeaderInner = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;

  width: 100%;
  max-width: 1280px;
`;

const LogoWrapper = styled.div`
  display: flex;
  align-items: center;
`;

const LogoBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 40px;
  height: 40px;
  margin-right: 12px;
  border-radius: 14px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  background: ${({ theme }) => theme.colors.primary};
`;

const TitleBox = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

const Title = styled.div`
  background: linear-gradient(144.324deg, #4b5563 0%, #f96 50%, #ffd700 100%);
  background-clip: text;

  font: ${({ theme }) => theme.fonts.heading4};

  -webkit-text-fill-color: transparent;
`;

const SubTitle = styled.div`
  font: ${({ theme }) => theme.fonts.body2};
`;

const Nav = styled.nav`
  display: flex;
  align-items: center;

  padding: 4px;
  border-radius: 14px;

  background: ${({ theme }) => theme.colors.white};

  gap: 8px;
`;

const NavButton = styled.button<{ active?: boolean }>`
  display: flex;
  align-items: center;

  padding: 10px 12px;
  border-radius: 12px;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};

  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  font: ${({ theme }) => theme.fonts.body2};

  gap: 4px;
`;

const ProfileBox = styled.div`
  display: flex;
  align-items: center;

  padding: 8px 12px;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.white};

  gap: 8px;
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

  font: ${({ theme }) => theme.fonts.caption};
`;

const ProfileName = styled.div``;

const ProfileEmail = styled.div`
  display: flex;
  align-items: center;

  cursor: pointer;
  gap: 4px;
`;

const EmailText = styled.div`
  font: ${({ theme }) => theme.fonts.caption};
`;
