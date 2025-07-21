import statusIcon from '../../../../../public/assets/reading-status.svg';
import streakIcon from '../../../../../public/assets/streak.svg';
import goalIcon from '../../../../../public/assets/goal.svg';
import styled from '@emotion/styled';
import {
  TodayReadingStatus,
  WeeklyReadingStatus,
} from '../../types/readingStatus';
import ProgressWithLabel from '../../../../components/ProgressWithLabel/ProgressWithLabel';

interface ReadingStatusCardProps {
  streakReadDay: number;
  today: TodayReadingStatus;
  weekly: WeeklyReadingStatus;
}

function ReadingStatusCard({
  streakReadDay,
  today: { readCount: todayReadCount, totalCount },
  weekly: { readCount: weeklyReadCount, goalCount },
}: ReadingStatusCardProps) {
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

      <ProgressWithLabel
        label="오늘의 진행률"
        icon={{ source: goalIcon, alternativeText: '목표 아이콘' }}
        value={{ currentCount: todayReadCount, totalCount }}
        description={
          todayReadCount < totalCount ? '목표까지 조금 더!' : '목표 달성!'
        }
      />
      <ProgressWithLabel
        label="주간 목표"
        icon={{ source: goalIcon, alternativeText: '목표 아이콘' }}
        value={{ currentCount: weeklyReadCount, totalCount: goalCount }}
        description={
          weeklyReadCount < goalCount
            ? `목표까지 ${goalCount - weeklyReadCount}개 남음`
            : '목표 달성!'
        }
        rateFormat="ratio"
      />
    </Container>
  );
}

export default ReadingStatusCard;

const Container = styled.section`
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  width: 310px;
  padding: 34px 30px;
  border: 1px solid ${({ theme }) => theme.colors.white};
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 15%);

  background-color: ${({ theme }) => theme.colors.white};

  gap: 26px;
`;

const TitleWrapper = styled.div`
  display: flex;
  align-items: center;

  width: 100%;

  gap: 10px;
`;

const StatusIconWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

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
  font: ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;

const StreakWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  gap: 10px;
`;

const StreakIconWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 70px;
  height: 70px;
  padding: 18px;
  border-radius: 36px;

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
