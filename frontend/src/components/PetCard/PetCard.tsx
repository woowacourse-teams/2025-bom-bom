import styled from '@emotion/styled';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { PET_LEVEL } from './PetCard.constants';
import { heartAnimation, jumpAnimation } from './PetCard.keyframes';
import Button from '../Button/Button';
import ProgressBar from '../ProgressBar/ProgressBar';
import Spacing from '../Spacing/Spacing';
import { getPet, postPetAttendance } from '@/apis/pet';
import { queryClient } from '@/main';
import { theme } from '@/styles/theme';
import { calculateRate } from '@/utils/math';
import PetIcon from '#/assets/pet.svg';

const PetCard = () => {
  const [isAnimating, setIsAnimating] = useState(false);

  const { data: pet } = useQuery({
    queryKey: ['pet'],
    queryFn: getPet,
  });

  const { mutate: mutatePetAttendance } = useMutation({
    mutationFn: postPetAttendance,
    onSuccess: () => {
      setIsAnimating(true);

      queryClient.invalidateQueries({ queryKey: ['pet'] });
    },
  });

  const levelPercentage = calculateRate(
    pet?.currentScore ?? 0,
    pet?.totalScore ?? 1,
  );

  const handleAttendanceClick = () => {
    mutatePetAttendance();
  };

  return (
    <Container>
      <TitleWrapper>
        <StatusIconWrapper>
          <PetIcon width={16} height={16} color={theme.colors.white} />
        </StatusIconWrapper>
        <Title>봄이</Title>
      </TitleWrapper>

      <Spacing size={16} />

      <PetImageContainer>
        <PetImage
          src={`./assets/pet-1-lv${pet?.level ?? 1}.png`}
          alt="pet"
          width={80}
          height={120}
          isAnimating={isAnimating}
          onAnimationEnd={() => setIsAnimating(false)}
        />
        {isAnimating && (
          <>
            {[...Array(5)].map((_, index) => (
              <Heart key={index} style={{ animationDelay: `${index * 0.1}s` }}>
                ❤️
              </Heart>
            ))}
          </>
        )}
      </PetImageContainer>

      <Level>
        레벨 {pet?.level} :{' '}
        {PET_LEVEL[(pet?.level ?? 1) as keyof typeof PET_LEVEL]}
      </Level>
      <Spacing size={16} />
      <ProgressBar rate={levelPercentage} caption={`${levelPercentage}%`} />
      <Button
        text={pet?.isAttended ? '출석 완료!' : '출석체크하기'}
        onClick={handleAttendanceClick}
        disabled={pet?.isAttended}
      />
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

const PetImageContainer = styled.div`
  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;
`;

const PetImage = styled.img<{ isAnimating: boolean }>`
  animation: ${({ isAnimating }) => (isAnimating ? jumpAnimation : 'none')} 0.6s
    ease-in-out;
`;

const Heart = styled.div`
  position: absolute;

  font-size: 20px;

  animation: ${heartAnimation} 1s ease-out forwards;

  pointer-events: none;

  &:nth-of-type(1) {
    left: 20%;
  }

  &:nth-of-type(2) {
    left: 40%;
  }

  &:nth-of-type(3) {
    left: 60%;
  }

  &:nth-of-type(4) {
    left: 80%;
  }

  &:nth-of-type(5) {
    left: 50%;
  }
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
