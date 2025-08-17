import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { RefObject } from 'react';
import { queries } from '@/apis/queries';
import Badge from '@/components/Badge/Badge';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import Modal from '@/components/Modal/Modal';
import { Newsletter } from '@/types/newsletter';
import ArticleHistoryIcon from '#/assets/article-history.svg';
import HomeIcon from '#/assets/home.svg';

interface NewsletterDetailModalProps {
  newsletter: Newsletter | null;
  modalRef: RefObject<HTMLDialogElement | null>;
  closeModal: () => void;
  clickOutsideModal: (event: React.MouseEvent<HTMLDialogElement>) => void;
}

export default function NewsletterDetailModal({
  newsletter,
  modalRef,
  closeModal,
  clickOutsideModal,
}: NewsletterDetailModalProps) {
  const { data: newsletterDetail } = useQuery(
    queries.newsletterDetail({ newsletterId: newsletter?.newsletterId ?? 0 }),
  );

  if (!newsletter || !newsletterDetail) return null;

  const goToSubscribe = () => {
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
    <Modal
      modalRef={modalRef}
      closeModal={closeModal}
      clickOutsideModal={clickOutsideModal}
      enableScroll={false}
    >
      <Container>
        <FixedWrapper>
          <InfoWrapper>
            <NewsletterImage
              src={newsletterDetail.imageUrl}
              alt={`${newsletterDetail.name} 뉴스레터 이미지`}
            />
            <InfoBox>
              <NewsletterTitle>{newsletterDetail.name}</NewsletterTitle>
              <NewsletterInfo>
                <Badge text={newsletter.category} />
                <IssueCycle>{newsletterDetail.issueCycle}</IssueCycle>
              </NewsletterInfo>
            </InfoBox>
          </InfoWrapper>

          <SubscribeButton text="구독하기" onClick={goToSubscribe} />
        </FixedWrapper>

        <ScrollableWrapper>
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

          {newsletterDetail.subscribePageImageUrl && (
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
                        {'봄봄을 통해 접속하였다면 즉시 붙여넣기가 가능합니다!'}
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
                <Screenshot
                  src={newsletterDetail.subscribePageImageUrl}
                  alt="구독 페이지 스크린샷"
                />
              </SubscribeContent>
            </SubscribeWrapper>
          )}
        </ScrollableWrapper>
      </Container>
    </Modal>
  );
}

const Container = styled.div`
  width: 100%;
  height: 100%;
  max-width: 560px;

  display: flex;
  flex-direction: column;
`;

const FixedWrapper = styled.div`
  padding-bottom: 24px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};

  display: flex;
  gap: 24px;
  flex-direction: column;
`;

const ScrollableWrapper = styled.div`
  padding-top: 24px;

  display: flex;
  gap: 24px;
  flex: 1;
  flex-direction: column;

  overflow-y: auto;
`;

const InfoWrapper = styled.div`
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
`;

const NewsletterImage = styled(ImageWithFallback)`
  width: 80px;
  height: 80px;
  border-radius: 16px;

  flex-shrink: 0;

  object-fit: cover;
`;

const InfoBox = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const NewsletterTitle = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const NewsletterInfo = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const IssueCycle = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
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

const SubscribeButton = styled(Button)`
  width: 100%;
  max-width: 400px;

  align-self: center;

  font: ${({ theme }) => theme.fonts.heading6};

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
  overflow: hidden;
  width: 100%;
  border-radius: 12px;
`;

const SubscribeContent = styled.div`
  overflow: hidden;
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
