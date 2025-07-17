import statusIcon from '../../../../public/assets/reading-status.svg';
import streakIcon from '../../../../public/assets/streak.svg';
import goalIcon from '../../../../public/assets/goal.svg';
import styled from '@emotion/styled';
import { calculateRate } from '../../../utils/math';
import ProgressBar from '../../../components/ProgressBar';

interface iconProps {
  source: string;
  alternativeText: string;
}

interface ReadingProgressBoxProps {
  label: string;
  rateString: string;
  progressRate: number;
  description: string;
  icon: iconProps;
}

interface TodayReadingStatus {
  readCount: number;
  totalCount: number;
}

interface WeeklyReadingStatus {
  readCount: number;
  goalCount: number;
}

interface ReadingStatusCardProps {
  streakReadDay: number;
  today: TodayReadingStatus;
  weekly: WeeklyReadingStatus;
}

function ReadingProgressBox({
  label,
  rateString,
  progressRate,
  description,
  icon: { source, alternativeText },
}: ReadingProgressBoxProps) {
  return (
    <ProgressContainer>
      <ProgressInfo>
        <img src={source} alt={alternativeText} />
        <ProgressLabel>{label}</ProgressLabel>
        <ProgressRate>{rateString}</ProgressRate>
      </ProgressInfo>
      <ProgressBar progressRate={progressRate} />
      <ProgressDescription>{description}</ProgressDescription>
    </ProgressContainer>
  );
}

function ReadingStatusCard({
  streakReadDay,
  today: { readCount: todayReadCount, totalCount },
  weekly: { readCount: weeklyReadCount, goalCount },
}: ReadingStatusCardProps) {
  const dailyProgressRate = calculateRate(todayReadCount, totalCount);
  const weeklyProgressRate = calculateRate(weeklyReadCount, goalCount);

  const DAILY_PROGRESS_PROPS = {
    label: '오늘의 진행률',
    rateString: `${dailyProgressRate}%`,
    progressRate: dailyProgressRate,
    description:
      todayReadCount < totalCount ? '목표까지 조금 더!' : '목표 달성!',
    icon: { source: goalIcon, alternativeText: '목표 아이콘' },
  };

  const WEEKLY_PROGRESS_PROPS = {
    label: '주간 목표',
    rateString: `${weeklyReadCount} / ${goalCount}`,
    progressRate: weeklyProgressRate,
    description:
      weeklyReadCount < goalCount
        ? `목표까지 ${goalCount - weeklyReadCount}개 남음`
        : '목표 달성!',
    icon: { source: goalIcon, alternativeText: '목표 아이콘' },
  };

  return (
    <Container>
      <TitleWrapper>
        <StatusIconWrapper>
          <img src={statusIcon} alt="현황 아이콘" />
        </StatusIconWrapper>
        <Title>읽기 현황</Title>
      </TitleWrapper>

      <StreakWrapper>
        <StreakIconWrapper>
          <StreakIconImage src={streakIcon} alt="연속 읽기 아이콘" />
        </StreakIconWrapper>
        <StreakDay>{`${streakReadDay}일`}</StreakDay>
        <StreakDescription>연속 읽기 중!🔥</StreakDescription>
        <StreakHelperText>Great Job!</StreakHelperText>
      </StreakWrapper>

      <ReadingProgressBox {...DAILY_PROGRESS_PROPS} />
      <ReadingProgressBox {...WEEKLY_PROGRESS_PROPS} />
    </Container>
  );
}

export default ReadingStatusCard;

const ProgressContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 14px;

  width: 100%;
`;

const ProgressInfo = styled.div`
  display: flex;
  gap: 6px;
`;

const ProgressLabel = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressRate = styled.span`
  margin-left: auto;

  color: ${({ theme }) => theme.colors.textPrimary};
  ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const ProgressDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  ${({ theme }) => theme.fonts.caption};
`;

const Container = styled.section`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 26px;

  width: 310px;
  padding: 34px 30px;
  border: 1px solid ${({ theme }) => theme.colors.white};
  border-radius: 20px;

  background-color: ${({ theme }) => theme.colors.white};
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 15%);
`;

const TitleWrapper = styled.div`
  display: flex;
  align-items: center;

  width: 100%;
  gap: 10px;
`;

const StatusIconWrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;

  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 14px;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const StreakIconImage = styled.img`
  filter: drop-shadow(0 0 8px rgb(255 153 102 / 50%))
    drop-shadow(0 0 16px rgb(255 153 102 / 30%))
    drop-shadow(0 0 16px rgb(255 153 102 / 20%));
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;

const StreakWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
`;

const StreakIconWrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;

  width: 70px;
  height: 70px;
  padding: 18px;
  border-radius: 36px;

  background-color: ${({ theme }) => theme.colors.primaryLight};
`;

const StreakDay = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font-size: ${({ theme }) => theme.fonts.heading3.fontSize};
  text-align: center;
`;

const StreakDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  ${({ theme }) => theme.fonts.body1};
  text-align: center;
`;

const StreakHelperText = styled.div`
  padding: 4px 8px;
  border-radius: 8px;

  color: ${({ theme }) => theme.colors.white};
  background-color: ${({ theme }) => theme.colors.primary};
  ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;
