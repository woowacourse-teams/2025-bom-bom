import styled from '@emotion/styled';
import { useState } from 'react';
import { createPortal } from 'react-dom';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import Modal from '@/components/Modal/Modal';
import useModal from '@/components/Modal/useModal';
import NewsletterUnsubscribeConfirmation from '@/pages/MyPage/NewsletterUnsubscribeConfirmation';
import { useUnsubscribeNewsletterMutation } from '@/pages/MyPage/useUnsubscribeNewsletterMutation';
import type { GetMyNewslettersResponse } from '@/apis/members';
import type { Device } from '@/hooks/useDevice';

interface SubscribedNewslettersSectionProps {
  newsletters: GetMyNewslettersResponse;
  device: Device;
}

const SubscribedNewslettersSection = ({
  newsletters,
  device,
}: SubscribedNewslettersSectionProps) => {
  const [selectedNewsletterId, setSelectedNewsletterId] = useState<
    number | null
  >(null);
  const { mutate: unsubscribeNewsletter } = useUnsubscribeNewsletterMutation();
  const {
    modalRef: UnsubscribeConfirmModalRef,
    openModal: openUnsubscribeConfirmModal,
    closeModal: closeUnsubscribeConfirmModal,
    isOpen,
  } = useModal();

  const handleOpenUnsubscribeModal = (newsletterId: number) => {
    setSelectedNewsletterId(newsletterId);
    openUnsubscribeConfirmModal();
  };

  const handleConfirmUnsubscribe = () => {
    if (!selectedNewsletterId) return;

    unsubscribeNewsletter({ subscriptionId: selectedNewsletterId });
    closeUnsubscribeConfirmModal();
    setSelectedNewsletterId(null);
  };
  return (
    <>
      <Container>
        {newsletters && newsletters.length > 0 ? (
          <NewsletterGrid device={device}>
            {newsletters.map((newsletter) => (
              <NewsletterCard key={newsletter.newsletterId} device={device}>
                <NewsletterContent>
                  <NewsletterImage
                    src={newsletter.imageUrl ?? ''}
                    alt={newsletter.name}
                    width={60}
                    height={60}
                  />
                  <NewsletterInfo>
                    <NewsletterName>{newsletter.name}</NewsletterName>
                    <NewsletterDescription>
                      {newsletter.description}
                    </NewsletterDescription>
                  </NewsletterInfo>
                </NewsletterContent>
                <UnsubscribeButton
                  type="button"
                  onClick={() =>
                    handleOpenUnsubscribeModal(newsletter.newsletterId)
                  }
                >
                  구독 해지
                </UnsubscribeButton>
              </NewsletterCard>
            ))}
          </NewsletterGrid>
        ) : (
          <EmptyMessage>구독 중인 뉴스레터가 없습니다.</EmptyMessage>
        )}
      </Container>
      {createPortal(
        <Modal
          modalRef={UnsubscribeConfirmModalRef}
          closeModal={closeUnsubscribeConfirmModal}
          isOpen={isOpen}
          showCloseButton={false}
        >
          <NewsletterUnsubscribeConfirmation
            onUnsubscribe={handleConfirmUnsubscribe}
            onClose={closeUnsubscribeConfirmModal}
          />
        </Modal>,
        document.body,
      )}
    </>
  );
};

export default SubscribedNewslettersSection;

const Container = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const NewsletterGrid = styled.div<{ device: Device }>`
  display: grid;
  gap: 16px;

  grid-template-columns: ${({ device }) =>
    device === 'mobile' ? '1fr' : 'repeat(auto-fill, minmax(300px, 1fr))'};
`;

const NewsletterCard = styled.div<{ device: Device }>`
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;

  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};

  transition: all 0.2s ease-in-out;
`;

const NewsletterContent = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
`;

const NewsletterImage = styled(ImageWithFallback)`
  width: 60px;
  height: 60px;
  border-radius: 8px;

  flex-shrink: 0;

  object-fit: cover;
`;

const NewsletterInfo = styled.div`
  overflow: hidden;

  display: flex;
  gap: 4px;
  flex: 1;
  flex-direction: column;
`;

const NewsletterName = styled.h3`
  overflow: hidden;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 600;
  white-space: nowrap;

  text-overflow: ellipsis;
`;

const NewsletterDescription = styled.p`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const EmptyMessage = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const UnsubscribeButton = styled.button`
  padding: 4px 8px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  align-self: flex-end;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body3};

  transition: all 0.2s ease-in-out;

  &:hover {
    background: ${({ theme }) => theme.colors.primaryLight};
  }
`;
