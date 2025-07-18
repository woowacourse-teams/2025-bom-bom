import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ReadingStatusCard from './ReadingStatusCard';

const meta = {
  title: 'components/bombom/ReadingStatusCard',
  component: ReadingStatusCard,
} satisfies Meta<typeof ReadingStatusCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    streakReadDay: 7,
    today: { readCount: 3, totalCount: 4 },
    weekly: { readCount: 4, goalCount: 5 },
  },
};

export const ReachedDailyGoal: Story = {
  args: {
    streakReadDay: 7,
    today: { readCount: 4, totalCount: 4 },
    weekly: { readCount: 4, goalCount: 5 },
  },
};

export const ReachedWeeklyGoal: Story = {
  args: {
    streakReadDay: 7,
    today: { readCount: 3, totalCount: 4 },
    weekly: { readCount: 5, goalCount: 5 },
  },
};

export const ReachedWholeGoal: Story = {
  args: {
    streakReadDay: 7,
    today: { readCount: 4, totalCount: 4 },
    weekly: { readCount: 5, goalCount: 5 },
  },
};
