import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import HomeIcon from '../../../public/assets/home.svg';
import { DeviceType } from '@/hooks/useDeviceType';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { theme } from '@/styles/theme';

interface HeaderLogoProps {
  deviceType: DeviceType;
}

const HeaderLogo = ({ deviceType }: HeaderLogoProps) => {
  return (
    <Container
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
        <HomeIcon width={24} height={24} color={theme.colors.white} />
      </LogoBox>
      <TitleBox>
        <Title>봄봄</Title>
        {deviceType === 'pc' && (
          <SubTitle>당신의 하루에 찾아오는 작은 설렘</SubTitle>
        )}
      </TitleBox>
    </Container>
  );
};

export default HeaderLogo;

const Container = styled(Link)`
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
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
