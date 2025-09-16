import styled from '@emotion/styled';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import maintenanceBom from '#/assets/maintenance-bom.png';

const MaintenanceCard = () => {
  return (
    <Container>
      <MaintenanceImage src={maintenanceBom} alt="점검 중 이미지" />

      <Title>서비스 점검 중입니다</Title>

      <DescriptionWrapper>
        <Description>
          더 나은 서비스 제공을 위해 시스템 점검을 진행하고 있습니다.
        </Description>
        <Description>
          잠시만 기다려 주시면 곧 정상 서비스로 돌아오겠습니다.
        </Description>
      </DescriptionWrapper>

      <ContactWrapper>
        <Contact href="https://e0pq0.channel.io/">💬 채널톡 바로가기</Contact>
        <ContactDescription>
          긴급한 문의사항이 있으시면
          <br />
          채널톡으로 연락해 주세요.
        </ContactDescription>
      </ContactWrapper>
    </Container>
  );
};

export default MaintenanceCard;

const Container = styled.section`
  width: 100%;
  height: 100dvh;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const MaintenanceImage = styled(ImageWithFallback)`
  width: 360px;
  height: 360px;
`;

const Title = styled.h1`
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const DescriptionWrapper = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const ContactWrapper = styled.div`
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgb(0 0 0 / 12%);

  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.white};
  text-align: center;
`;

const Contact = styled.a`
  padding: 8px 16px;
  border-radius: 12px;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body1};

  text-decoration: none;

  transition: all 0.2s ease;

  &:hover {
    background: ${({ theme }) => theme.colors.primary};
    color: ${({ theme }) => theme.colors.white};
  }
`;

const ContactDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body3};
`;
