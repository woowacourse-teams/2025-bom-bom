import styled from '@emotion/styled';
import ProgressBar from '../ProgressBar/ProgressBar';
import petImage from '#/assets/pet-example.png';

const PetCard = () => {
  return (
    <Container>
      <TitleWrapper>
        <StatusIconWrapper>
          {/* <StatusIcon width={20} height={20} /> */}
        </StatusIconWrapper>
        <Title>읽기 현황</Title>
      </TitleWrapper>
      <img src={petImage} alt="pet" width={160} height={160} />
      <p>현재 레벨</p>
      <ProgressBar rate={70} />
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
  gap: 16px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
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
