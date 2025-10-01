import {
  AttendanceButton,
  Container,
  PetImageContainer,
  StatusIconWrapper,
  Title,
  TitleWrapper,
} from './PetCard';
import ProgressBarSkeleton from '../ProgressBar/ProgressBarSkeleton';
import Skeleton from '../Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';
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
        <Skeleton width="120px" height="120px" borderRadius="12px" />
      </PetImageContainer>

      <Skeleton width="100px" height="16px" />

      <ProgressBarSkeleton hasCaption={true} />

      <Skeleton width="50%" height="100%" as={AttendanceButton} />
    </Container>
  );
};

export default PetCardSkeleton;
