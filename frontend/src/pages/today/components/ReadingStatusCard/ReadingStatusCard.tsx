import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import ReadingStatusCardContainer from './ReadingStatusCardContainer';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import WeeklyGoalEditor, { WeeklyGoalInput } from './WeeklyGoalEditor';
import StreakCounter from '../StreakCounter/StreakCounter';
import { queries } from '@/apis/queries';
import ProgressBar from '@/components/ProgressBar/ProgressBar';
import ProgressWithLabel from '@/components/ProgressWithLabel/ProgressWithLabel';
import { useDevice } from '@/hooks/useDevice';
import useUpdateWeeklyGoalMutation from '@/pages/today/hooks/useUpdateWeeklyGoalMutation';
import { theme } from '@/styles/theme';
import type { Device } from '@/hooks/useDevice';
import type { CSSObject, Theme } from '@emotion/react';
import GoalIcon from '#/assets/svg/goal.svg';
import StatusIcon from '#/assets/svg/reading-status.svg';

function ReadingStatusCard() {
  const device = useDevice();
  const { data } = useQuery(queries.readingStatus());
  const [isEditing, setIsEditing] = useState(false);
  const [goalCount, setGoalCount] = useState<number | null>(null);

  const { mutate: updateWeeklyGoal, isPending } = useUpdateWeeklyGoalMutation({
    onSuccess: () => {
      setIsEditing(false);
    },
  });

  if (!data) return null;

  const { streakReadDay, today, weekly } = data;

  const handleEditStart = () => {
    setIsEditing(true);
    setGoalCount(weekly.goalCount);
  };

  const handleSave = () => {
    if (goalCount === null) return;

    if (goalCount !== weekly.goalCount) {
      updateWeeklyGoal({
        weeklyGoalCount: goalCount,
      });
    } else {
      setIsEditing(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setGoalCount(weekly.goalCount);
  };

  const handleGoalChange = (value: number | null) => {
    setGoalCount(value);
  };
  const todayProgressDescription =
    today.readCount < today.totalCount ? '목표까지 조금 더!' : '목표 달성!';
  const weeklyGoalDescription =
    weekly.readCount < weekly.goalCount
      ? `목표까지 ${weekly.goalCount - weekly.readCount}개 남음`
      : '목표 달성!';

  return (
    <ReadingStatusCardContainer>
      <StreakWrapper device={device}>
        <StreakCounter streakReadDay={streakReadDay} />
        <StreakDescription device={device}>연속 읽기 중!</StreakDescription>
      </StreakWrapper>

      <ProgressWithLabel
        label="오늘의 진행률"
        value={{
          currentCount: today.readCount,
          totalCount: today.totalCount,
        }}
        {...(device === 'pc'
          ? { Icon: GoalIcon, description: todayProgressDescription }
          : { showGraph: false })}
      />
      <WeeklyGoalSection>
        <WeeklyProgressContainer>
          <ProgressInfo>
            {device === 'pc' && <StyledIcon as={GoalIcon} />}
            <ProgressLabel>주간 목표</ProgressLabel>
            <WeeklyGoalEditor
              isEditing={isEditing}
              isPending={isPending}
              device={device}
              onEditStart={handleEditStart}
              onSave={handleSave}
            />
            {isEditing ? (
              <InputContainer>
                <span>{weekly.readCount}/</span>
                <WeeklyGoalInput
                  goalValue={goalCount}
                  isPending={isPending}
                  device={device}
                  onSave={handleSave}
                  onCancel={handleCancel}
                  onGoalChange={handleGoalChange}
                />
              </InputContainer>
            ) : (
              <ProgressRate>{`${weekly.readCount}/${weekly.goalCount}`}</ProgressRate>
            )}
          </ProgressInfo>
          {device === 'pc' && (
            <ProgressBar rate={(weekly.readCount / weekly.goalCount) * 100} />
          )}
          {device === 'pc' && weeklyGoalDescription && (
            <ProgressDescription>{weeklyGoalDescription}</ProgressDescription>
          )}
        </WeeklyProgressContainer>
      </WeeklyGoalSection>
    </ReadingStatusCardContainer>
  );
}

export default ReadingStatusCard;

const StreakDescription = styled.p<{ device: Device }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ device, theme }) =>
    device === 'pc' ? theme.fonts.body1 : theme.fonts.body2};
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

export const StreakWrapper = styled.div<{ device: Device }>`
  display: flex;
  gap: ${({ device }) => (device === 'pc' ? '8px' : '0px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;
