import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import ReadingStatusCardSkeleton from './ReadingStatusCardSkeleton';
import { queries } from '@/apis/queries';
import ProgressWithLabel from '@/components/ProgressWithLabel/ProgressWithLabel';
import { theme } from '@/styles/theme';
import GoalIcon from '#/assets/goal.svg';
import StatusIcon from '#/assets/reading-status.svg';
import StreakIcon from '#/assets/streak.svg';

function ReadingStatusCard() {
  const { data, isLoading } = useQuery(queries.readingStatus());

  if (isLoading) return <ReadingStatusCardSkeleton />;
  if (!data) return null;

  const { streakReadDay, today, weekly } = data;

  return (
    <Container>
      <TitleWrapper>
        <StatusIconWrapper>
          <StatusIcon width={20} height={20} color={theme.colors.white} />
        </StatusIconWrapper>
        <Title>읽기 현황</Title>
      </TitleWrapper>

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

      <ProgressWithLabel
        label="오늘의 진행률"
        Icon={GoalIcon}
        value={{
          currentCount: today?.readCount ?? 0,
          totalCount: today?.totalCount ?? 0,
        }}
        description={
          today?.readCount &&
          today?.totalCount &&
          today?.readCount < today?.totalCount
            ? '목표까지 조금 더!'
            : '목표 달성!'
        }
      />
      <ProgressWithLabel
        label="주간 목표"
        Icon={GoalIcon}
        value={{
          currentCount: weekly?.readCount ?? 0,
          totalCount: weekly?.goalCount ?? 0,
        }}
        description={
          weekly?.readCount &&
          weekly?.goalCount &&
          weekly?.readCount < weekly?.goalCount
            ? `목표까지 ${weekly?.goalCount - weekly?.readCount}개 남음`
            : '목표 달성!'
        }
        rateFormat="ratio"
      />
    </Container>
  );
}

export default ReadingStatusCard;

const Container = styled.section`
  width: 310px;
  padding: 34px 30px;
  border: 1px solid ${({ theme }) => theme.colors.white};
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 15%);

  display: flex;
  gap: 26px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
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
