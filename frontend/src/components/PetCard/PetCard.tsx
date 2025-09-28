import styled from '@emotion/styled';
import { useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { LEVEL, PET_LABEL, PET_WIDTH } from './PetCard.constants';
import { heartAnimation, jumpAnimation } from './PetCard.keyframes';
import Button from '../Button/Button';
import ProgressBar from '../ProgressBar/ProgressBar';
import { postPetAttendance } from '@/apis/pet';
import { useDevice } from '@/hooks/useDevice';
import { queryClient } from '@/main';
import { theme } from '@/styles/theme';
import { calculateRate } from '@/utils/math';
import type { GetPetResponse } from '@/apis/pet';
import type { Device } from '@/hooks/useDevice';
import type { CSSObject, Theme } from '@emotion/react';
import petLv1 from '#/assets/avif/pet-1-lv1.avif';
import petLv2 from '#/assets/avif/pet-1-lv2.avif';
import petLv3 from '#/assets/avif/pet-1-lv3.avif';
import petLv4 from '#/assets/avif/pet-1-lv4.avif';
import petLv5 from '#/assets/avif/pet-1-lv5.avif';
import PetIcon from '#/assets/svg/pet.svg';

const petImages: Record<number, string> = {
  1: petLv1,
  2: petLv2,
  3: petLv3,
  4: petLv4,
  5: petLv5,
};

interface PetCardProps {
  pet: GetPetResponse;
  isLoading: boolean;
}

const PetCard = ({ pet, isLoading }: PetCardProps) => {
  const device = useDevice();
  const [isAnimating, setIsAnimating] = useState(false);

  const { mutate: mutatePetAttendance } = useMutation({
    mutationFn: postPetAttendance,
    onSuccess: () => {
      setIsAnimating(true);

      queryClient.invalidateQueries({ queryKey: ['pet'] });
    },
  });

  if (!isLoading && !pet) return null;

  const { level, isAttended, currentStageScore, requiredStageScore } = pet;
  const levelPercentage = calculateRate(currentStageScore, requiredStageScore);

  const handleAttendanceClick = () => {
    mutatePetAttendance();
  };

  return (
    <Container device={device}>
      {device === 'pc' && (
        <TitleWrapper>
          <StatusIconWrapper>
            <PetIcon width={16} height={16} color={theme.colors.white} />
          </StatusIconWrapper>
          <Title>봄이</Title>
        </TitleWrapper>
      )}
      <PetImageContainer>
        <PetImage
          src={petImages[level]}
          alt="pet"
          width={PET_WIDTH[level]}
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
        레벨 {level} : {PET_LABEL[level]}
      </Level>

      <ProgressBar
        rate={level === LEVEL.max ? 100 : levelPercentage}
        caption={
          level === LEVEL.max ? `${currentStageScore}점` : `${levelPercentage}%`
        }
      />

      <AttendanceButton
        device={device}
        text={isAttended ? '출석 완료!' : '출석체크하기'}
        onClick={handleAttendanceClick}
        disabled={isAttended}
      />
    </Container>
  );
};

export default PetCard;

export const Container = styled.section<{ device: Device }>`
  width: 310px;
  border-radius: 20px;

  display: flex;
  gap: 16px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  ${({ device, theme }) => containerStyles[device](theme)}
`;

export const PetImageContainer = styled.div`
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

export const TitleWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 10px;
  align-items: center;
`;

export const StatusIconWrapper = styled.div`
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

export const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;

export const AttendanceButton = styled(Button)<{ device: Device }>`
  height: 32px;

  ${({ device }) =>
    device === 'mobile' && {
      position: 'absolute',
      left: '50%',
      bottom: 0,
      width: '50%',
      transform: 'translateX(-50%)',
    }}
`;

const containerStyles: Record<Device, (theme: Theme) => CSSObject> = {
  pc: () => ({
    padding: '34px 30px',
    border: `1px solid ${theme.colors.white}`,
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
    backgroundColor: theme.colors.white,
  }),
  tablet: () => ({
    flex: '1',
  }),
  mobile: () => ({
    flex: '1',
  }),
};
