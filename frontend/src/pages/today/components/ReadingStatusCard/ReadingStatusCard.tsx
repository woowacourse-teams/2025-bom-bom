import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import WeeklyGoalEditor from './WeeklyGoalEditor';
import StreakCounter from '../StreakCounter/StreakCounter';
import { queries } from '@/apis/queries';
import ProgressWithLabel from '@/components/ProgressWithLabel/ProgressWithLabel';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import useUpdateWeeklyGoalMutation from '@/pages/today/hooks/useUpdateWeeklyGoalMutation';
import { theme } from '@/styles/theme';
import type { CSSObject, Theme } from '@emotion/react';
import GoalIcon from '#/assets/goal.svg';
import StatusIcon from '#/assets/reading-status.svg';

function ReadingStatusCard() {
  const deviceType = useDeviceType();
  const { data, isLoading } = useQuery(queries.readingStatus());
  const [isEditing, setIsEditing] = useState(false);
  const [goal, setGoal] = useState<number | null>(null);

  const { mutate: updateWeeklyGoal, isPending } = useUpdateWeeklyGoalMutation({
    onSuccess: () => {
      setIsEditing(false);
    },
  });

  if (isLoading) return <ReadingStatusCardSkeleton />;
  if (!data) return null;

  const { streakReadDay, today, weekly } = data;

  const handleEditStart = () => {
    setIsEditing(true);
    setGoal(weekly.goalCount);
  };

  const handleSave = () => {
    if (goal === null) return;

    if (goal !== weekly.goalCount) {
      updateWeeklyGoal({
        memberId: 17, // TODO: 실제 회원 ID로 변경 필요
        weeklyGoalCount: goal,
      });
    } else {
      setIsEditing(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setGoal(weekly.goalCount);
  };

  const handleGoalChange = (value: number | null) => {
    setGoal(value);
  };
  const todayProgressDescription =
    today.readCount < today.totalCount ? '목표까지 조금 더!' : '목표 달성!';
  const weeklyGoalDescription =
    weekly.readCount < weekly.goalCount
      ? `목표까지 ${weekly.goalCount - weekly.readCount}개 남음`
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

      <StreakWrapper deviceType={deviceType}>
        <StreakCounter streakReadDay={streakReadDay} />
        <StreakDescription deviceType={deviceType}>
          연속 읽기 중!
        </StreakDescription>
      </StreakWrapper>

      <ProgressWithLabel
        label="오늘의 진행률"
        value={{
          currentCount: today.readCount,
          totalCount: today.totalCount,
        }}
        {...(deviceType === 'pc'
          ? { Icon: GoalIcon, description: todayProgressDescription }
          : { showGraph: false })}
      />
      <WeeklyGoalSection>
        <ProgressWithLabel
          label="주간 목표"
          value={{
            currentCount: weekly.readCount,
            totalCount: weekly.goalCount,
          }}
          rateFormat="ratio"
          {...(deviceType === 'pc'
            ? { Icon: GoalIcon, description: weeklyGoalDescription }
            : { showGraph: false })}
        />
        <WeeklyGoalEditor
          isEditing={isEditing}
          goalValue={goal}
          isPending={isPending}
          deviceType={deviceType}
          onEditStart={handleEditStart}
          onSave={handleSave}
          onCancel={handleCancel}
          onGoalChange={handleGoalChange}
        />
      </WeeklyGoalSection>
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
  align-items: center;
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

const StreakDescription = styled.p<{ deviceType: DeviceType }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ deviceType, theme }) =>
    deviceType === 'pc' ? theme.fonts.body1 : theme.fonts.body2};
  text-align: center;
`;

const WeeklyGoalSection = styled.div`
  position: relative;
  width: 100%;
`;

const containerStyles: Record<DeviceType, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    padding: '34px 30px',
    backgroundColor: theme.colors.white,
    border: `1px solid ${theme.colors.white}`,
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
  }),
  tablet: () => ({
    height: '200px',
    flex: '1',
    gap: '12px',
  }),
  mobile: () => ({
    height: '200px',
    flex: '1',
    gap: '12px',
  }),
};
