import styled from '@emotion/styled';
import { theme } from '@bombom/shared/theme';
import {
  Container,
  StatusIconWrapper,
  Title,
  TitleWrapper,
} from './ReadingStatusCard';
import ProgressWithLabelSkeleton from '@/components/ProgressWithLabel/ProgressWithLabelSkeleton';
import Skeleton from '@/components/Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';
import StatusIcon from '#/assets/svg/reading-status.svg';

const ReadingStatusCardSkeleton = () => {
  const device = useDevice();
  const isPC = device === 'pc';

  return (
    <Container device={device}>
      {device === 'pc' && (
        <TitleWrapper>
          <StatusIconWrapper>
            <StatusIcon width={20} height={20} color={theme.colors.white} />
          </StatusIconWrapper>
          <Title>읽기 현황</Title>
        </TitleWrapper>
      )}

      <Skeleton width="120px" height="120px" borderRadius="12px" />

      <SkeletonProgressWrapper isPC={isPC}>
        <ProgressWithLabelSkeleton
          hasShowGraph={isPC}
          hasShowDescription={isPC}
        />

        <ProgressWithLabelSkeleton
          hasShowGraph={isPC}
          hasShowDescription={isPC}
        />
      </SkeletonProgressWrapper>
    </Container>
  );
};

export default ReadingStatusCardSkeleton;

const SkeletonProgressWrapper = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isPC }) => (isPC ? '50px' : '16px')};
  flex-direction: column;
`;
