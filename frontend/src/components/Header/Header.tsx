import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from '@tanstack/react-router';
import HomeIcon from '../../../public/assets/home.svg';
import Button from '../Button/Button';
import { queries } from '@/apis/queries';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { theme } from '@/styles/theme';
import { NavType } from '@/types/nav';
import { copyToClipboard } from '@/utils/copy';
import defaultImage from '#/assets/bombom.png';
import CompassIcon from '#/assets/compass.svg';
import CopyIcon from '#/assets/copy.svg';
import StorageIcon from '#/assets/storage.svg';

interface HeaderProps {
  activeNav: NavType;
}

export default function Header({ activeNav }: HeaderProps) {
  const navigate = useNavigate();
  const deviceType = useDeviceType();
  const { data: userInfo, isFetching } = useQuery(queries.me());

  const handleCopyEmail = () => {
    if (!userInfo?.email) return;

    alert(`이메일이 복사되었습니다.`);
    copyToClipboard(userInfo?.email);
  };

  console.log(deviceType);

  const isLoggedIn = isFetching || userInfo;

  return deviceType === 'mobile' ? (
    <>
      <MobileHeaderBar>
        <LogoWrapper
          to="/"
          onClick={() =>
            trackEvent({
              category: 'Navigation',
              action: 'Click Logo (Mobile)',
              label: 'Go to Home',
            })
          }
        >
          <LogoBox>
            <HomeIcon width={22} height={22} color={theme.colors.white} />
          </LogoBox>
          <Title>봄봄</Title>
        </LogoWrapper>

        <ProfileWrapper>
          {isLoggedIn ? (
            <ProfileInfo>
              <ProfileImg
                src={userInfo?.profileImageUrl ?? defaultImage}
                alt="profile"
                width={32}
                height={32}
              />
              <ProfileTextBox>
                <ProfileName>{userInfo?.nickname}</ProfileName>
                <ProfileEmail onClick={handleCopyEmail}>
                  <EmailText>{userInfo?.email}</EmailText>
                  <CopyIcon width={16} height={16} />
                </ProfileEmail>
              </ProfileTextBox>
            </ProfileInfo>
          ) : (
            <Button
              text="로그인"
              onClick={() => {
                navigate({ to: '/login' });
              }}
            />
          )}
        </ProfileWrapper>
      </MobileHeaderBar>

      <BottomNavWrapper>
        <BottomNavItem
          to="/"
          active={activeNav === 'today'}
          onClick={() =>
            trackEvent({
              category: 'Navigation',
              action: 'Click Today Nav (Bottom)',
              label: 'Go to Today',
            })
          }
        >
          <HomeIcon
            width={22}
            height={22}
            color={
              activeNav === 'today' ? theme.colors.white : theme.colors.black
            }
          />
          <span>오늘</span>
        </BottomNavItem>

        <BottomNavItem
          to="/storage"
          active={activeNav === 'storage'}
          onClick={() =>
            trackEvent({
              category: 'Navigation',
              action: 'Click Storage Nav (Bottom)',
              label: 'Go to Storage',
            })
          }
        >
          <StorageIcon
            width={22}
            height={22}
            color={
              activeNav === 'storage' ? theme.colors.white : theme.colors.black
            }
          />
          <span>보관함</span>
        </BottomNavItem>

        <BottomNavItem
          to="/recommend"
          active={activeNav === 'recommend'}
          onClick={() =>
            trackEvent({
              category: 'Navigation',
              action: 'Click Recommend Nav (Bottom)',
              label: 'Go to Recommend',
            })
          }
        >
          <CompassIcon
            width={22}
            height={22}
            color={
              activeNav === 'recommend'
                ? theme.colors.white
                : theme.colors.black
            }
          />
          <span>추천</span>
        </BottomNavItem>
      </BottomNavWrapper>

      {/* 본문이 헤더/바텀탭과 겹치지 않도록 여백 가이드 */}
      <MobileTopSpacer />
      <MobileBottomSpacer />
    </>
  ) : (
    <HeaderContainer>
      <HeaderInner>
        <LogoWrapper
          to="/"
          onClick={() =>
            trackEvent({
              category: 'Navigation',
              action: 'Click Logo',
              label: 'Go to Home',
            })
          }
        >
          <LogoBox>
            <HomeIcon width={24} height={24} color={theme.colors.white} />
          </LogoBox>
          <TitleBox>
            <Title>봄봄</Title>
            {deviceType === 'pc' && (
              <SubTitle>당신의 하루에 찾아오는 작은 설렘</SubTitle>
            )}
          </TitleBox>
        </LogoWrapper>

        <Nav deviceType={deviceType}>
          <NavButton
            active={activeNav === 'today'}
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
        </Nav>

        <ProfileWrapper>
          {isLoggedIn ? (
            <ProfileInfo>
              <ProfileImg
                src={userInfo?.profileImageUrl ?? defaultImage}
                alt="profile"
                width={32}
                height={32}
              />
              <ProfileTextBox>
                <ProfileName>{userInfo?.nickname}</ProfileName>
                <ProfileEmail onClick={handleCopyEmail}>
                  <EmailText>{userInfo?.email}</EmailText>
                  <CopyIcon width={16} height={16} />
                </ProfileEmail>
              </ProfileTextBox>
            </ProfileInfo>
          ) : (
            <Button
              text="로그인"
              onClick={() => {
                navigate({ to: '/login' });
              }}
            />
          )}
        </ProfileWrapper>
      </HeaderInner>
    </HeaderContainer>
  );
}

const MobileHeaderBar = styled.header`
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

const BottomNavItem = styled(Link)<{ active?: boolean }>`
  height: 48px;
  margin: 0 4px;
  border-radius: 12px;

  display: flex;
  gap: 4px;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};
  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  font: ${({ theme }) => theme.fonts.caption};
`;

const MobileTopSpacer = styled.div`
  height: 56px; /* MobileHeaderBar 높이만큼 */
`;

const MobileBottomSpacer = styled.div`
  height: calc(
    64px + env(safe-area-inset-bottom)
  ); /* BottomNavWrapper 높이만큼 */
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

const LogoWrapper = styled(Link)`
  display: flex;
  align-items: center;
`;

const LogoBox = styled.div`
  width: 40px;
  height: 40px;
  margin-right: 12px;
  border-radius: 14px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  display: flex;
  align-items: center;
  justify-content: center;

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

const Nav = styled.nav<{ deviceType: DeviceType }>`
  padding: 4px;
  border-radius: 14px;

  display: flex;
  gap: ${({ deviceType }) => (deviceType !== 'pc' ? '4px' : '8px')};
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
`;

const NavButton = styled(Link)<{ active?: boolean }>`
  padding: 10px 12px;
  border-radius: 12px;

  display: flex;
  gap: 4px;
  align-items: center;

  background: ${({ active, theme }) =>
    active ? theme.colors.primary : 'transparent'};
  color: ${({ active, theme }) =>
    active ? theme.colors.white : theme.colors.black};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ProfileWrapper = styled.div`
  min-width: 200px;
  padding: 8px 12px;
  border-radius: 12px;

  display: flex;
  gap: 8px;
  align-items: center;

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
