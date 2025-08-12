import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import { queries } from '@/apis/queries';
import ProgressWithLabel from '@/components/ProgressWithLabel/ProgressWithLabel';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import GoalIcon from '#/assets/goal.svg';
import StatusIcon from '#/assets/reading-status.svg';
import StreakIcon from '#/assets/streak.svg';

function ReadingStatusCard() {
  const device = useDeviceType();
  const { data, isLoading } = useQuery(queries.readingStatus());

  if (isLoading) return <ReadingStatusCardSkeleton />;
  if (!data) return null;

  const { streakReadDay, today, weekly } = data;
  const todayProgressDescription =
    today?.readCount &&
    today?.totalCount &&
    today?.readCount < today?.totalCount
      ? '목표까지 조금 더!'
      : '목표 달성!';
  const weeklyProgressDescription =
    weekly?.readCount &&
    weekly?.goalCount &&
    weekly?.readCount < weekly?.goalCount
      ? `목표까지 ${weekly?.goalCount - weekly?.readCount}개 남음`
      : '목표 달성!';

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

      {device === 'pc' ? (
        <StreakWrapper>
          <StreakIconWrapper>
            <StreakIcon
              width={34}
              height={34}
              fill={theme.colors.white}
              color={theme.colors.primary}
            />
          </StreakIconWrapper>
          <StreakDay>{`${streakReadDay}일`}</StreakDay>
          <StreakDescription>연속 읽기 중!🔥</StreakDescription>
          <StreakHelperText>Great Job!</StreakHelperText>
        </StreakWrapper>
      ) : (
        <StreakWrapperMobile>
          <StreakIcon
            width={16}
            height={16}
            fill={theme.colors.white}
            color={theme.colors.primary}
          />
          <StreakDayMobile>{`${streakReadDay}일 연속 읽기 중!`}</StreakDayMobile>
        </StreakWrapperMobile>
      )}

      <ProgressWithLabel
        label="오늘의 진행률"
        Icon={GoalIcon}
        value={{
          currentCount: today?.readCount ?? 0,
          totalCount: today?.totalCount ?? 0,
        }}
        {...(device === 'pc' ? { description: todayProgressDescription } : {})}
      />
      <ProgressWithLabel
        label="주간 목표"
        Icon={GoalIcon}
        value={{
          currentCount: weekly?.readCount ?? 0,
          totalCount: weekly?.goalCount ?? 0,
        }}
        {...(device === 'pc' ? { description: weeklyProgressDescription } : {})}
        rateFormat="ratio"
      />
    </Container>
  );
}

export default ReadingStatusCard;

const Container = styled.section<{ device: DeviceType }>`
  width: 310px;
  padding: 34px 30px;
  border: ${({ device, theme }) =>
    device === 'mobile' ? 'none' : `1px solid ${theme.colors.white}`};
  border-radius: 20px;
  box-shadow: ${({ device }) =>
    device === 'mobile' ? 'none' : '0 25px 50px -12px rgb(0 0 0 / 15%)'};

  display: flex;
  gap: 26px;
  flex: ${({ device }) => (device === 'mobile' ? '1' : 'auto')};
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;

  background-color: ${({ device, theme }) =>
    device === 'mobile' ? 'transparent' : `${theme.colors.white}`};
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

const StreakWrapper = styled.div`
  display: flex;
  gap: 10px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const StreakIconWrapper = styled.div`
  width: 70px;
  height: 70px;
  padding: 18px;
  border-radius: 36px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primaryLight};
`;

const StreakDay = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font-size: 28px;
  text-align: center;
`;

const StreakDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
  text-align: center;
`;

const StreakHelperText = styled.div`
  padding: 4px 8px;
  border-radius: 8px;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const StreakWrapperMobile = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  text-align: center;
`;

const StreakDayMobile = styled.p`
  font: ${({ theme }) => theme.fonts.body2};
`;
