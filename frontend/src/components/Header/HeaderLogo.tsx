import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import { Device } from '@/hooks/useDeviceType';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import logo from '#/assets/logo.png';

interface HeaderLogoProps {
  deviceType: Device;
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
      <Logo src={logo} alt="logo" />
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

const Logo = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 16px;
`;

const Container = styled(Link)`
  display: flex;
  gap: 12px;
  align-items: center;
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
