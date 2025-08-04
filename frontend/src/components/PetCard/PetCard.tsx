import styled from '@emotion/styled';
import Button from '../Button/Button';
import ProgressBar from '../ProgressBar/ProgressBar';
import Spacing from '../Spacing/Spacing';
import { theme } from '@/styles/theme';
import petImage from '#/assets/pet-1-lv1.png';
import PetIcon from '#/assets/pet.svg';

const PetCard = () => {
  return (
    <Container>
      <TitleWrapper>
        <StatusIconWrapper>
          <PetIcon width={16} height={16} color={theme.colors.white} />
        </StatusIconWrapper>
        <Title>몽이</Title>
      </TitleWrapper>

      <Spacing size={16} />

      <img src={petImage} alt="pet" width={100} height={120} />
      <Level>레벨 2 : 아직 애기</Level>
      <Spacing size={16} />
      <ProgressBar rate={70} caption="70%" />
      <Button text="맘마 먹이기" onClick={() => {}} />
    </Container>
  );
};

export default PetCard;

const Container = styled.section`
  width: 310px;
  padding: 34px 30px;
  border: 1px solid ${({ theme }) => theme.colors.white};
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 15%);

  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
`;

const Level = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const TitleWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 10px;
  align-items: center;
`;

const StatusIconWrapper = styled.div`
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;
