import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import StreakCounter from '../StreakCounter/StreakCounter';
import { queries } from '@/apis/queries';
import ProgressWithLabel from '@/components/ProgressWithLabel/ProgressWithLabel';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import { calculateRate } from '@/utils/math';
import type { CSSObject, Theme } from '@emotion/react';
import GoalIcon from '#/assets/goal.svg';
import StatusIcon from '#/assets/reading-status.svg';
import StreakIcon from '#/assets/streak.svg';

function ReadingStatusCard() {
  const deviceType = useDeviceType();
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
    <Container deviceType={deviceType}>
      {deviceType === 'pc' && (
        <TitleWrapper>
          <StatusIconWrapper>
            <StatusIcon width={20} height={20} color={theme.colors.white} />
          </StatusIconWrapper>
          <Title>읽기 현황</Title>
        </TitleWrapper>
      )}

      {deviceType === 'pc' ? (
        <>
          <StreakWrapper deviceType={deviceType}>
            <StreakIconWrapper>
              <StreakIcon
                width={34}
                height={34}
                fill={theme.colors.white}
                color={theme.colors.primary}
              />
            </StreakIconWrapper>
            <StreakDay>{`${streakReadDay}일`}</StreakDay>
            <StreakDescription deviceType={deviceType}>
              연속 읽기 중!🔥
            </StreakDescription>
            <StreakHelperText>Great Job!</StreakHelperText>
          </StreakWrapper>

          <ProgressWithLabel
            label="오늘의 진행률"
            Icon={GoalIcon}
            value={{
              currentCount: today?.readCount ?? 0,
              totalCount: today?.totalCount ?? 0,
            }}
            {...(deviceType === 'pc'
              ? { description: todayProgressDescription }
              : {})}
          />
          <ProgressWithLabel
            label="주간 목표"
            Icon={GoalIcon}
            value={{
              currentCount: weekly?.readCount ?? 0,
              totalCount: weekly?.goalCount ?? 0,
            }}
            {...(deviceType === 'pc'
              ? { description: weeklyProgressDescription }
              : {})}
            rateFormat="ratio"
          />
        </>
      ) : (
        <StatisticWrapper>
          <StreakWrapper deviceType={deviceType}>
            <StreakCounter streakReadDay={streakReadDay ?? 0} />
            <StreakDescription deviceType={deviceType}>
              연속 읽기 중!
            </StreakDescription>
          </StreakWrapper>

          <GoalWrapper>
            <Goal>
              오늘의 진행률
              <ReachedGoal>{`${calculateRate(
                weekly?.readCount ?? 0,
                weekly?.goalCount ?? 0,
              )}%`}</ReachedGoal>
            </Goal>
            <Goal>
              주간 목표
              <Rate>
                <ReachedGoal>{weekly?.readCount ?? 0}</ReachedGoal>
                <TotalGoal>{` / ${today?.totalCount ?? 0}`}</TotalGoal>
              </Rate>
            </Goal>
          </GoalWrapper>
        </StatisticWrapper>
      )}
    </Container>
  );
}

export default ReadingStatusCard;

const Container = styled.section<{ deviceType: DeviceType }>`
  width: 310px;
  border-radius: 20px;

  display: flex;
  gap: 26px;
  flex-direction: column;
  justify-content: center;

  ${({ deviceType, theme }) => containerStyles[deviceType](theme)}
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

const StreakWrapper = styled.div<{ deviceType: DeviceType }>`
  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'pc' ? '8px' : '0px')};
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

const StreakDescription = styled.p<{ deviceType: DeviceType }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ deviceType, theme }) =>
    deviceType === 'pc' ? theme.fonts.body1 : theme.fonts.body2};
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

const StatisticWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const GoalWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  text-align: center;
`;

const Goal = styled.p`
  width: 100%;

  display: flex;
  justify-content: space-between;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const Rate = styled.div`
  display: flex;
`;

const ReachedGoal = styled.span`
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const TotalGoal = styled.span`
  font: ${({ theme }) => theme.fonts.body2};
`;

const containerStyles: Record<DeviceType, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    padding: '34px 30px',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    border: `1px solid ${theme.colors.white}`,
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
  }),
  tablet: () => ({
    flex: '1',
    alignItems: 'flex-start',
  }),
  mobile: () => ({
    flex: '1',
    alignItems: 'flex-start',
  }),
};
