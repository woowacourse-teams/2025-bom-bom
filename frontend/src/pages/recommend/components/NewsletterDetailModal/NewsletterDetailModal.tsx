import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { Link } from '@tanstack/react-router';
import { RefObject } from 'react';
import { queries } from '@/apis/queries';
import Badge from '@/components/Badge/Badge';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import Modal from '@/components/Modal/Modal';
import { Newsletter } from '@/types/newsletter';
import LinkIcon from '#/assets/link.svg';

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

  return (
    <Modal
      modalRef={modalRef}
      closeModal={closeModal}
      clickOutsideModal={clickOutsideModal}
    >
      <Container>
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

        <Description>{newsletterDetail.description}</Description>

        <ButtonWrapper>
          <Button text="구독하기" onClick={goToSubscribe} />
          <MainSiteButton onClick={goToMainSite}>
            <LinkIcon width={16} height={16} />
            메인 사이트
          </MainSiteButton>
        </ButtonWrapper>

        {newsletterDetail.subscribePageImageUrl && (
          <SubscribeWrapper>
            <SubscribeTitle>구독 방법</SubscribeTitle>
            <Screenshot
              src={newsletterDetail.subscribePageImageUrl}
              alt="구독 페이지 스크린샷"
            />
          </SubscribeWrapper>
        )}

        {newsletterDetail.previousNewsletterUrl && (
          <SubscribeWrapper>
            <SubscribeTitle>지난 뉴스레터</SubscribeTitle>
            <PastNewsletterLink
              href={newsletterDetail.previousNewsletterUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              이전 뉴스레터 보기
            </PastNewsletterLink>
          </SubscribeWrapper>
        )}
      </Container>
    </Modal>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 560px;

  display: flex;
  gap: 24px;
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

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const ButtonWrapper = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const MainSiteButton = styled.button`
  padding: 8px 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  gap: 6px;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  transition: all 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
    border-color: ${({ theme }) => theme.colors.primary};
  }
`;

const SubscribeWrapper = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
`;

const SubscribeTitle = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
`;

const IssueCycle = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const Screenshot = styled(ImageWithFallback)`
  overflow: hidden;
  width: 100%;
  border-radius: 12px;
`;

const PastNewsletterLink = styled(Link)`
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: inline-block;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};

  text-decoration: none;

  transition: all 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
    border-color: ${({ theme }) => theme.colors.primary};
  }
`;
