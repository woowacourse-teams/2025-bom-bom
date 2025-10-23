import styled from '@emotion/styled';
import { useState } from 'react';
import PromotionBanner from '../PromotionBanner/PromotionBanner';
import QRCodeModal from '../QRCodeModal/QRCodeModal';
import Carousel from '@/components/Carousel/Carousel';
import useModal from '@/components/Modal/useModal';
import { useDevice } from '@/hooks/useDevice';
import type { StoreType } from '../PromotionBanner/PromotionBanner.types';

const BANNERS = [
  {
    src: '/assets/avif/banner_1.avif',
    alt: '디그, 디그와 함께 하세요. 누구보다 쉽게 핵심을 디깅해 드릴게요',
  },
  {
    src: '/assets/avif/banner_2.avif',
    alt: '캐릿, 트렌드 당일 배송, 렛츠 캐릿',
  },
  {
    src: '/assets/avif/banner_3.avif',
    alt: '데일리 바이트, 놓치면 안 될 비즈니스, 경제 이슈 한 눈에 보기',
  },
  {
    src: '/assets/avif/banner_4.avif',
    alt: '부딩, 구독형 부동산 뉴스레터',
  },
];

const SlideCardList = () => {
  const device = useDevice();
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
      <Carousel isInfinity={true} autoPlay={true}>
        {device === 'pc' && <PromotionBanner openModal={handleOpenModal} />}
        {BANNERS.map(({ src, alt }, index) => (
          <Banner key={`banner-${index}`} src={src} alt={alt} />
        ))}
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

const Banner = styled.img`
  width: 100%;
  height: 280px;

  object-fit: contain;
  object-position: center;
`;
