import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { createPortal } from 'react-dom';
import NewsletterHero from '../../pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';
import ReadingKingLeaderboard from '../../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import TrendySection from '../../pages/recommend/components/ReadingKingLeaderboard/TrendySection/TrendySection';
import Modal from '@/components/Modal/Modal';
import useModal from '@/components/Modal/useModal';

export const Route = createFileRoute('/_bombom/recommend')({
  component: Recommend,
});

function Recommend() {
  const { modalRef, openModal, closeModal, clickOutsideModal } = useModal();

  return (
    <Container>
      <MainSection>
        <NewsletterHero />
        <TrendySection openModal={openModal} />
      </MainSection>
      <SideSection>
        <ReadingKingLeaderboard />
      </SideSection>
      {createPortal(
        <Modal
          modalRef={modalRef}
          closeModal={closeModal}
          clickOutsideModal={clickOutsideModal}
        >
          {'모달 컨텐츠'}
        </Modal>,
        document.body,
      )}
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 20px 0;

  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: center;

  @media (width <= 768px) {
    flex-direction: column;
    align-items: center;
  }
`;

const MainSection = styled.section`
  max-width: 840px;

  display: flex;
  gap: 24px;
  flex: 1;
  flex-direction: column;
`;

const SideSection = styled.div`
  width: 400px;
  flex-shrink: 0;

  @media (width <= 768px) {
    width: 100%;
    max-width: 400px;
  }
`;
