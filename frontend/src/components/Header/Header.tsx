import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { getUserInfo } from '@/apis/members';
import { NavType } from '@/types/nav';
import { copyToClipboard } from '@/utils/copy';
import defaultImage from '#/assets/bombom.png';
import CompassIcon from '#/assets/compass.svg';
import CopyIcon from '#/assets/copy.svg';
import HomeIcon from '#/assets/home.svg';
import StorageIcon from '#/assets/storage.svg';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav }: HeaderProps) {
  const navagate = useNavigate();
  const {
    data: userInfo,
    isError,
    isFetching,
  } = useQuery({
    queryKey: ['userInfo'],
    queryFn: () => getUserInfo(),
  });

  const handleCopyEmail = () => {
    if (!userInfo?.email) return;

    alert(`이메일이 복사되었습니다.`);
    copyToClipboard(userInfo?.email);
  };

  return (
    <HeaderContainer>
      <HeaderInner>
        <LogoWrapper to="/">
          <LogoBox>
            <HomeIcon width={24} height={24} color="white" />
          </LogoBox>
          <TitleBox>
            <Title>봄봄</Title>
            <SubTitle>당신의 하루에 찾아오는 작은 설렘</SubTitle>
          </TitleBox>
        </LogoWrapper>

        <Nav>
          <NavButton active={activeNav === 'today'} to="/">
            <HomeIcon
              width={24}
              height={24}
              color={activeNav === 'today' ? 'white' : 'black'}
            />
            <p>오늘의 뉴스레터</p>
          </NavButton>
          <NavButton active={activeNav === 'storage'} to="/storage">
            <StorageIcon color={activeNav === 'storage' ? 'white' : 'black'} />
            <p>뉴스레터 보관함</p>
          </NavButton>
          <NavButton active={activeNav === 'recommend'} to="/recommend">
            <CompassIcon
              color={activeNav === 'recommend' ? 'white' : 'black'}
            />
            <p>뉴스레터 추천</p>
          </NavButton>
        </Nav>

        <ProfileWrapper>
          {isFetching || isError ? (
            <Button
              text="로그인"
              onClick={() => {
                navagate({ to: '/login' });
              }}
            />
          ) : (
            <ProfileInfo>
              <ProfileImg
                src={userInfo?.profileImageUrl ?? defaultImage}
                alt="profile"
              />
              <ProfileTextBox>
                <ProfileName>{userInfo?.nickname ?? '김봄봄'}</ProfileName>
                <ProfileEmail onClick={handleCopyEmail}>
                  <EmailText>
                    {userInfo?.email ?? 'example@bombom.news'}
                  </EmailText>
                  <CopyIcon width={16} height={16} />
                </ProfileEmail>
              </ProfileTextBox>
            </ProfileInfo>
          )}
        </ProfileWrapper>
      </HeaderInner>
    </HeaderContainer>
  );
}

const HeaderContainer = styled.header`
  position: fixed;
  top: 0;
  z-index: 100;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 100%;
  height: 72px;
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

const LogoWrapper = styled(Link)`
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
  gap: 8px;
  align-items: center;

  padding: 4px;
  border-radius: 14px;

  background: ${({ theme }) => theme.colors.white};
`;

const NavButton = styled(Link)<{ active?: boolean }>`
  display: flex;
  gap: 4px;
  align-items: center;

  padding: 10px 12px;
  border-radius: 12px;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};

  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ProfileWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;

  padding: 8px 12px;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.white};
`;

const ProfileImg = styled.img`
  width: 34px;
  height: 34px;
  border-radius: 50%;
`;

const ProfileTextBox = styled.div`
  display: flex;
  flex-direction: column;
`;

const ProfileInfo = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  font: ${({ theme }) => theme.fonts.caption};
`;

const ProfileName = styled.div``;

const ProfileEmail = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;

  cursor: pointer;
`;

const EmailText = styled.div`
  font: ${({ theme }) => theme.fonts.caption};
`;
