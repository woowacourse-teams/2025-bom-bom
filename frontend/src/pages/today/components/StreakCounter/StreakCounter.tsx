import styled from '@emotion/styled';

interface StreakIconProps {
  streakReadDay: number;
}

const StreakCounter = ({ streakReadDay }: StreakIconProps) => {
  return <CircleIcon>{`${streakReadDay}일`}</CircleIcon>;
};

export default StreakCounter;

const CircleIcon = styled.span`
  position: relative;
  z-index: 0;
  width: 68px;
  height: 68px;
  margin: 4px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.heading6};

  &::before {
    position: absolute;
    top: 50%;
    left: 50%;
    z-index: -1;
    width: calc(100% - 16px);
    height: calc(100% - 16px);
    border-radius: 50%;

    background-color: ${({ theme }) => theme.colors.white};

    content: '';
    transform: translate(-50%, -50%);
  }

  > * {
    position: relative;
    z-index: 1;
  }
`;
