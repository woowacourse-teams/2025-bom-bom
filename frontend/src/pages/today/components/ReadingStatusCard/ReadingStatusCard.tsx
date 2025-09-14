import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import WeeklyGoalEditor, { WeeklyGoalInput } from './WeeklyGoalEditor';
import StreakCounter from '../StreakCounter/StreakCounter';
import { queries } from '@/apis/queries';
import ProgressBar from '@/components/ProgressBar/ProgressBar';
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
        <WeeklyProgressContainer>
          <ProgressInfo>
            {deviceType === 'pc' && <StyledIcon as={GoalIcon} />}
            <ProgressLabel>주간 목표</ProgressLabel>
            <WeeklyGoalEditor
              isEditing={isEditing}
              isPending={isPending}
              deviceType={deviceType}
              onEditStart={handleEditStart}
              onSave={handleSave}
            />
            {isEditing ? (
              <InputContainer>
                <span>{weekly.readCount}/</span>
                <WeeklyGoalInput
                  goalValue={goal}
                  isPending={isPending}
                  deviceType={deviceType}
                  onSave={handleSave}
                  onCancel={handleCancel}
                  onGoalChange={handleGoalChange}
                />
              </InputContainer>
            ) : (
              <ProgressRate>{`${weekly.readCount}/${weekly.goalCount}`}</ProgressRate>
            )}
          </ProgressInfo>
          {deviceType === 'pc' && (
            <ProgressBar rate={(weekly.readCount / weekly.goalCount) * 100} />
          )}
          {deviceType === 'pc' && weeklyGoalDescription && (
            <ProgressDescription>{weeklyGoalDescription}</ProgressDescription>
          )}
        </WeeklyProgressContainer>
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

const WeeklyGoalSection = styled.section`
  position: relative;
  width: 100%;
`;

const WeeklyProgressContainer = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;

const ProgressInfo = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  align-items: center;
`;

const StyledIcon = styled.img`
  width: 16px;
  height: 16px;

  object-fit: cover;
  object-position: center;
`;

const ProgressLabel = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressRate = styled.span`
  margin-left: auto;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const InputContainer = styled.div`
  margin-left: auto;

  display: flex;
  gap: 0;
  align-items: center;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};

  span {
    color: ${({ theme }) => theme.colors.primary};
    font: ${({ theme }) => theme.fonts.body2};
  }
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
