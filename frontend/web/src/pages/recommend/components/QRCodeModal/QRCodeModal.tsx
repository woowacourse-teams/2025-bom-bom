import { APP_STORE_LINK, PLAY_STORE_LINK } from '@bombom/shared';
import styled from '@emotion/styled';
import QRCode from 'react-qr-code';
import Modal from '@/components/Modal/Modal';
import type { StoreType } from '../PromotionBanner/PromotionBanner.types';

interface QRCodeModalProps {
  modalRef: (node: HTMLDivElement) => void;
  isOpen: boolean;
  onClose: () => void;
  storeType: StoreType | null;
}

const QRCodeModal = ({
  modalRef,
  isOpen,
  onClose,
  storeType,
}: QRCodeModalProps) => {
  const getStoreInfo = () => {
    if (storeType === 'appStore') {
      return { link: APP_STORE_LINK, title: 'App Store 앱 설치' };
    }
    return { link: PLAY_STORE_LINK, title: 'Google Play 앱 설치' };
  };

  const storeInfo = getStoreInfo();

  return (
    <Modal
      modalRef={modalRef}
      isOpen={isOpen && storeType !== null}
      closeModal={onClose}
    >
      <Container>
        <QRModalTitle>{storeInfo.title}</QRModalTitle>
        <QRCodeWrapper>
          <QRCode size={256} value={storeInfo.link} viewBox="0 0 256 256" />
        </QRCodeWrapper>
        <QRModalDescription>
          QR 코드를 스캔하여 앱을 설치하세요
        </QRModalDescription>
      </Container>
    </Modal>
  );
};

export default QRCodeModal;

const Container = styled.div`
  padding: 60px 48px 48px;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: center;
`;

const QRModalTitle = styled.h2`
  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const QRCodeWrapper = styled.div`
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
`;

const QRModalDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
