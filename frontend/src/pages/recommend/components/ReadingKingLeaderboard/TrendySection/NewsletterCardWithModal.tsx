import styled from '@emotion/styled';
import { createPortal } from 'react-dom';
import NewsletterDetailModal from '../../NewsletterDetailModal/NewsletterDetailModal';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import useModal from '@/components/Modal/useModal';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { Newsletter } from '@/types/newsletter';

interface NewsletterCardWithModalProps {
  newsletter: Newsletter;
}

const NewsletterCardWithModal = ({
  newsletter,
}: NewsletterCardWithModalProps) => {
  const {
    modalRef: detailModalRef,
    openModal: openDetailModal,
    closeModal: closeDetailModal,
    clickOutsideModal: clickOutsideDetailModal,
    isOpen,
  } = useModal();

  const handleCardClick = () => {
    openDetailModal();
    trackEvent({
      category: 'Newsletter',
      action: 'Click Trendy Newsletter Card',
      label: newsletter.name ?? 'Unknown Newsletter',
    });
  };

  return (
    <>
      <NewsletterCard
        imageUrl={newsletter.imageUrl ?? ''}
        title={newsletter.name}
        description={newsletter.description}
        onClick={handleCardClick}
        as="button"
      />
      {isOpen &&
        createPortal(
          <NewsletterDetailModal
            newsletterId={newsletter.newsletterId}
            category={newsletter.category}
            modalRef={detailModalRef}
            closeModal={closeDetailModal}
            clickOutsideModal={clickOutsideDetailModal}
            isOpen={isOpen}
          />,
          document.body,
        )}
    </>
  );
};

export default NewsletterCardWithModal;

const NewsletterCard = styled(ImageInfoCard)`
  &:focus-visible {
    outline: none;
  }

  &:focus:not(:focus-visible) {
    outline: none;
  }
`;
