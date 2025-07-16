import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ReadingProgressBox from '../pages/today/components/ReadingProgressBox';

const meta = {
  title: 'ReadingProgressBox',
  component: ReadingProgressBox,
} satisfies Meta<typeof ReadingProgressBox>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    label: '제목',
    rateCaption: '진행률',
    progressRate: 75,
    description: '설명 텍스트',
  },
};

export const DailyProgress: Story = {
  args: {
    label: '오늘의 진행률',
    rateCaption: '75%',
    progressRate: 75,
    description: '목표까지 조금 더!',
  },
};

export const WeeklyProgress: Story = {
  args: {
    label: '주간 목표',
    rateCaption: '4/5',
    progressRate: 80,
    description: '목표까지 1개 남음',
  },
};
