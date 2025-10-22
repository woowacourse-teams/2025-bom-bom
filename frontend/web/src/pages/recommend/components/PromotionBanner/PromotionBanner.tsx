import styled from '@emotion/styled';
import type { StoreType } from './PromotionBanner.types';
import appleIcon from '#/assets/avif/apple.avif';
import bookBomIcon from '#/assets/avif/book_bom.avif';
import playStoreIcon from '#/assets/avif/play_store.avif';

interface PromotionBannerProps {
  openModal: (storeType: StoreType) => void;
}

const PromotionBanner = ({ openModal }: PromotionBannerProps) => {
  return (
    <Container>
      <Content>
        <TextBox>
          <Text>뉴스레터</Text>
          <Text>더 이상 놓치지 않도록</Text>
          <StoreButtons>
            <StoreButton onClick={() => openModal('appStore')}>
              <img src={appleIcon} alt="App Store" width={20} height={20} />
              App Store
            </StoreButton>
            <StoreButton onClick={() => openModal('playStore')}>
              <img
                src={playStoreIcon}
                alt="Google Play"
                width={20}
                height={20}
              />
              Google Play
            </StoreButton>
          </StoreButtons>
        </TextBox>
        <Character src={bookBomIcon} width={200} alt="봄이 캐릭터" />
      </Content>
    </Container>
  );
};

export default PromotionBanner;

const Container = styled.div`
  width: 100%;
  height: 280px;

  display: flex;
  align-items: center;
  justify-content: center;

  box-sizing: border-box;
`;

const Content = styled.div`
  width: 560px;
  padding: 0 10px 0 40px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const TextBox = styled.div``;

const Text = styled.h2`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const StoreButtons = styled.div`
  margin-top: 18px;

  display: flex;
  gap: 12px;
`;

const StoreButton = styled.button`
  width: 128px;
  padding: 12px 18px;
  border-radius: 8px;

  display: flex;
  gap: 8px;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body3};

  &:hover {
    transform: scale(1.05);
  }
`;

const Character = styled.img``;
