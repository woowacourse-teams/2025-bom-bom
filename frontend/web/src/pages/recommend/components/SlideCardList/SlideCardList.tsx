import styled from '@emotion/styled';
import { useState } from 'react';
import PromotionBanner from '../PromotionBanner/PromotionBanner';
import QRCodeModal from '../QRCodeModal/QRCodeModal';
import Carousel from '@/components/Carousel/Carousel';
import useModal from '@/components/Modal/useModal';
import type { StoreType } from '../PromotionBanner/PromotionBanner.types';

const banner1 = '/assets/avif/banner_1.avif';
const banner2 = '/assets/avif/banner_2.avif';
const banner3 = '/assets/avif/banner_3.avif';
const banner4 = '/assets/avif/banner_4.avif';

const SlideCardList = () => {
  const { modalRef, closeModal, isOpen, openModal } = useModal();
  const [storeType, setStoreType] = useState<StoreType | null>(null);

  const handleOpenModal = (type: StoreType) => {
    setStoreType(type);
    openModal();
  };

  const handleCloseModal = () => {
    setStoreType(null);
    closeModal();
  };

  return (
    <>
      <Carousel isInfinity={true} autoPlay={false}>
        <PromotionBanner openModal={handleOpenModal} />
        <Banner imageUrl={banner1} />
        <Banner imageUrl={banner2} />
        <Banner imageUrl={banner3} />
        <Banner imageUrl={banner4} />
      </Carousel>
      <QRCodeModal
        modalRef={modalRef}
        isOpen={isOpen}
        onClose={handleCloseModal}
        storeType={storeType}
      />
    </>
  );
};

export default SlideCardList;

const Banner = styled.div<{ imageUrl: string }>`
  width: 100%;
  height: 280px;

  background-image: url(${({ imageUrl }) => imageUrl});
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
`;
