import styled from '@emotion/styled';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { PET_LEVEL } from './PetCard.constants';
import { heartAnimation, jumpAnimation } from './PetCard.keyframes';
import Button from '../Button/Button';
import ProgressBar from '../ProgressBar/ProgressBar';
import { getPet, postPetAttendance } from '@/apis/pet';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { queryClient } from '@/main';
import { theme } from '@/styles/theme';
import { calculateRate } from '@/utils/math';
import type { CSSObject, Theme } from '@emotion/react';
import petLv1 from '#/assets/pet-1-lv1.png';
import petLv2 from '#/assets/pet-1-lv2.png';
import petLv3 from '#/assets/pet-1-lv3.png';
import petLv4 from '#/assets/pet-1-lv4.png';
import petLv5 from '#/assets/pet-1-lv5.png';
import PetIcon from '#/assets/pet.svg';

const petImages: Record<number, string> = {
  1: petLv1,
  2: petLv2,
  3: petLv3,
  4: petLv4,
  5: petLv5,
};

const PetCard = () => {
  const deviceType = useDeviceType();
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
    pet?.currentStageScore ?? 0,
    pet?.requiredStageScore ?? 1,
  );

  const handleAttendanceClick = () => {
    mutatePetAttendance();
  };

  return (
    <Container deviceType={deviceType}>
      {deviceType === 'pc' && (
        <TitleWrapper>
          <StatusIconWrapper>
            <PetIcon width={16} height={16} color={theme.colors.white} />
          </StatusIconWrapper>
          <Title>봄이</Title>
        </TitleWrapper>
      )}

      <PetImageContainer>
        <PetImage
          src={petImages[pet?.level ?? 1]}
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

      <ProgressBar rate={levelPercentage} caption={`${levelPercentage}%`} />

      <AttendanceButton
        deviceType={deviceType}
        text={pet?.isAttended ? '출석 완료!' : '출석체크하기'}
        onClick={handleAttendanceClick}
        disabled={pet?.isAttended}
      />
    </Container>
  );
};

export default PetCard;

const Container = styled.section<{ deviceType: DeviceType }>`
  width: 310px;
  border-radius: 20px;

  display: flex;
  gap: 16px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  ${({ deviceType, theme }) => containerStyles[deviceType](theme)}
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

const AttendanceButton = styled(Button)<{ deviceType: DeviceType }>`
  height: 32px;

  ${({ deviceType }) =>
    deviceType === 'mobile' && {
      position: 'absolute',
      left: '50%',
      bottom: 0,
      width: '50%',
      transform: 'translateX(-50%)',
    }}
`;

const containerStyles: Record<DeviceType, (theme: Theme) => CSSObject> = {
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
