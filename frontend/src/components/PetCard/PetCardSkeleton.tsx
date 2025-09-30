import styled from '@emotion/styled';
import {
  AttendanceButton,
  Container,
  PetImageContainer,
  StatusIconWrapper,
  Title,
  TitleWrapper,
} from './PetCard';
import ProgressBarSkeleton from '../ProgressBar/ProgressBarSkeleton';
import SkeletonBox from '../Skeleton/SkeletonBox';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';
import { theme } from '@/styles/theme';
import PetIcon from '#/assets/svg/pet.svg';

const PetCardSkeleton = () => {
  const device = useDevice();

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
        <SkeletonPetImage />
      </PetImageContainer>

      <SkeletonBox width="100px" height="16px" />

      <ProgressBarSkeleton hasCaption={true} />

      <SkeletonAttendanceButton as={AttendanceButton} />
    </Container>
  );
};

export default PetCardSkeleton;

const SkeletonPetImage = styled.div`
  width: 120px;
  height: 120px;
  border-radius: 12px;

  ${skeletonStyle}
`;

const SkeletonAttendanceButton = styled.div`
  width: 50%;

  ${skeletonStyle};
`;
