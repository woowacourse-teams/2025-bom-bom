import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import Badge from '@/components/Badge/Badge';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { useDeviceType } from '@/hooks/useDeviceType';
import { copyToClipboard } from '@/utils/copy';
import ArticleHistoryIcon from '#/assets/article-history.svg';
import HomeIcon from '#/assets/home.svg';

interface NewsletterDetailProps {
  newsletterId: number;
  category: string;
}

const NewsletterDetail = ({
  newsletterId,
  category,
}: NewsletterDetailProps) => {
  const { data: userInfo } = useQuery(queries.me());
  const { data: newsletterDetail } = useQuery({
    ...queries.newsletterDetail({ id: newsletterId }),
    enabled: Boolean(newsletterId),
  });
  const deviceType = useDeviceType();
  const isMobile = deviceType === 'mobile';

  if (!newsletterId || !newsletterDetail) return null;

  const goToSubscribe = async () => {
    if (userInfo?.email) {
      await copyToClipboard(userInfo.email);
      alert('이메일이 복사되었습니다. 이 이메일로 뉴스레터를 구독해주세요.');
    }

    window.open(newsletterDetail.subscribeUrl, '_blank', 'noopener,noreferrer');
  };

  const goToMainSite = () => {
    window.open(newsletterDetail.mainPageUrl, '_blank', 'noopener,noreferrer');
  };

  const goToPreviousLetters = () => {
    window.open(
      newsletterDetail.previousNewsletterUrl,
      '_blank',
      'noopener,noreferrer',
    );
  };

  return (
    <Container>
      <FixedWrapper isMobile={isMobile}>
        <InfoWrapper isMobile={isMobile}>
          <NewsletterImage
            src={newsletterDetail.imageUrl}
            alt={`${newsletterDetail.name} 뉴스레터 이미지`}
            isMobile={isMobile}
          />
          <InfoBox>
            <NewsletterTitle isMobile={isMobile}>
              {newsletterDetail.name}
            </NewsletterTitle>
            <NewsletterInfo>
              <Badge text={category} />
              <IssueCycle isMobile={isMobile}>
                {newsletterDetail.issueCycle}
              </IssueCycle>
            </NewsletterInfo>
          </InfoBox>
        </InfoWrapper>

        <SubscribeButton
          text="구독하기"
          onClick={goToSubscribe}
          isMobile={isMobile}
        />
      </FixedWrapper>

      <ScrollableWrapper isMobile={isMobile}>
        <Description>{newsletterDetail.description}</Description>

        <LinkWrapper>
          <DetailLink onClick={goToMainSite}>
            <HomeIcon width={18} height={18} />
            홈페이지
          </DetailLink>

          {newsletterDetail.previousNewsletterUrl && (
            <DetailLink onClick={goToPreviousLetters}>
              <ArticleHistoryIcon width={18} height={18} />
              지난 소식 보기
            </DetailLink>
          )}
        </LinkWrapper>

        {deviceType !== 'mobile' && (
          <SubscribeWrapper>
            <SubscribeHeader>
              <SubscribeTitle>구독 방법</SubscribeTitle>
            </SubscribeHeader>
            <SubscribeContent>
              <StepsWrapper>
                <StepItem>
                  <StepNumber>1</StepNumber>
                  <StepContent>
                    <StepTitle>구독하기 버튼 클릭</StepTitle>
                    <StepDescription>
                      {'위의 "구독하기" 버튼을 눌러주세요.'}
                    </StepDescription>
                  </StepContent>
                </StepItem>
                <StepItem>
                  <StepNumber>2</StepNumber>
                  <StepContent>
                    <StepTitle>구독 페이지 접속</StepTitle>
                    <StepDescription>
                      {'뉴스레터 공식 구독 페이지로 이동합니다.'}
                    </StepDescription>
                  </StepContent>
                </StepItem>
                <StepItem>
                  <StepNumber>3</StepNumber>
                  <StepContent>
                    <StepTitle>봄봄 메일 붙여넣기</StepTitle>
                    <StepDescription>
                      {'이메일 칸에 봄봄 메일을 입력해주세요.'}
                    </StepDescription>
                    <StepDescription>
                      {
                        '봄봄을 통해 접속한 유저라면 즉시 붙여넣기가 가능합니다!'
                      }
                    </StepDescription>
                  </StepContent>
                </StepItem>
                <StepItem>
                  <StepNumber>4</StepNumber>
                  <StepContent>
                    <StepTitle>구독 완료!</StepTitle>
                    <StepDescription>
                      {'축하합니다! 이제 정기적으로 뉴스레터를 받아보세요.'}
                    </StepDescription>
                  </StepContent>
                </StepItem>
              </StepsWrapper>
              {newsletterDetail.subscribePageImageUrl && (
                <Screenshot
                  src={newsletterDetail.subscribePageImageUrl}
                  alt="구독 페이지 스크린샷"
                />
              )}
            </SubscribeContent>
          </SubscribeWrapper>
        )}
      </ScrollableWrapper>
    </Container>
  );
};

export default NewsletterDetail;

const Container = styled.div`
  width: 100%;
  height: 100%;
  max-width: 560px;

  display: flex;
  flex-direction: column;
`;

const FixedWrapper = styled.div<{ isMobile: boolean }>`
  padding-bottom: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  flex-direction: column;
`;

const ScrollableWrapper = styled.div<{ isMobile: boolean }>`
  min-height: 0;
  margin-right: -16px;
  padding-top: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  padding-right: 16px;

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  flex-direction: column;

  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
`;

const InfoWrapper = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '12px' : '16px')};
  align-items: center;
  justify-content: center;
`;

const NewsletterImage = styled(ImageWithFallback)<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '60px' : '80px')};
  height: ${({ isMobile }) => (isMobile ? '60px' : '80px')};
  border-radius: ${({ isMobile }) => (isMobile ? '12px' : '16px')};

  flex-shrink: 0;

  object-fit: cover;
`;

const InfoBox = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const NewsletterTitle = styled.h2<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.heading5 : theme.fonts.heading4};
`;

const NewsletterInfo = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const IssueCycle = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.caption : theme.fonts.body2};
  text-align: center;
`;

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const LinkWrapper = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const DetailLink = styled.a`
  display: flex;
  gap: 4px;
  align-items: center;

  transition: all 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
    cursor: pointer;
  }
`;

const SubscribeButton = styled(Button)<{ isMobile: boolean }>`
  width: 100%;
  max-width: 400px;

  align-self: center;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.heading6};

  transition: all 0.2s ease;

  &:hover {
    filter: brightness(90%);
  }
`;

const SubscribeWrapper = styled.div`
  padding: 8px 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const SubscribeHeader = styled.div`
  width: 100%;
  padding: 8px 0;
`;

const SubscribeTitle = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
`;

const Screenshot = styled(ImageWithFallback)`
  width: 100%;
  border-radius: 12px;

  object-fit: cover;
  object-position: center;
`;

const SubscribeContent = styled.div`
  width: 100%;
  padding: 16px;
  border-top: 1px solid ${({ theme }) => theme.colors.dividers};
`;

const StepsWrapper = styled.div`
  position: relative;
  padding: 20px 16px;

  &::before {
    position: absolute;
    top: 36px;
    bottom: 68px;
    left: 32px;
    width: 2px;

    background: ${({ theme }) => theme.colors.dividers};

    content: '';
  }
`;

const StepItem = styled.div`
  position: relative;
  margin-bottom: 32px;

  display: flex;
  gap: 16px;
  align-items: flex-start;

  &:last-child {
    margin-bottom: 0;
  }
`;

const StepNumber = styled.span`
  z-index: 1;
  width: 32px;
  height: 32px;
  border: 2px solid ${({ theme }) => theme.colors.white};
  border-radius: 50%;
  box-shadow: 0 2px 8px rgb(0 0 0 / 10%);

  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
`;

const StepContent = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;

const StepTitle = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const StepDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
