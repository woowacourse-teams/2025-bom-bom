import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ProgressWithLabel from './ProgressWithLabel';

const meta = {
  title: 'components/common/ProgressWithLabel',
  component: ProgressWithLabel,
} satisfies Meta<typeof ProgressWithLabel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    label: '[제목]',
    value: {
      currentCount: 3,
      totalCount: 4,
    },
    description: '[설명]',
    icon: {
      source:
        'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
      alternativeText: '대한민국 국기',
    },
  },
};

export const DailyProgress: Story = {
  args: {
    label: '오늘의 진행률',
    value: {
      currentCount: 3,
      totalCount: 4,
    },
    description: '목표까지 조금 더!',
    icon: {
      source:
        'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
      alternativeText: '대한민국 국기',
    },
  },
};

export const WeeklyProgress: Story = {
  args: {
    label: '주간 목표',
    value: {
      currentCount: 3,
      totalCount: 4,
    },
    description: '목표까지 1개 남음',
    icon: {
      source:
        'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
      alternativeText: '대한민국 국기',
    },
    rateFormat: 'ratio',
  },
};
