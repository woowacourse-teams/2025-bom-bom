import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ReadingKingLeaderboard from './ReadingKingLeaderboard';

const meta: Meta<typeof ReadingKingLeaderboard> = {
  title: 'components/ReadingKingLeaderboard',
  component: ReadingKingLeaderboard,
  parameters: {
    layout: 'padded',
  },
};
export default meta;

type Story = StoryObj<typeof ReadingKingLeaderboard>;

export const Default: Story = {
  render: () => <ReadingKingLeaderboard />,
};

export const CustomData: Story = {
  render: () => (
    <ReadingKingLeaderboard
      data={[
        {
          id: 1,
          rank: 1,
          name: '홍길동',
          avatar: 'https://via.placeholder.com/35/87CEEB/000000?text=홍',
          readCount: 150,
          increment: 10,
          isCrown: true,
          badgeText: '👑 챔피언',
        },
        {
          id: 2,
          rank: 2,
          name: '이몽룡',
          avatar: 'https://via.placeholder.com/35/FFB6C1/000000?text=이',
          readCount: 120,
          increment: 8,
          isCrown: false,
        },
        {
          id: 3,
          rank: 3,
          name: '성춘향',
          avatar: 'https://via.placeholder.com/35/DDA0DD/000000?text=성',
          readCount: 100,
          increment: 5,
          isCrown: false,
        },
      ]}
      userRank={{
        rank: 5,
        readCount: 45,
        nextRankDifference: 20,
        progressPercentage: 40,
      }}
    />
  ),
};

export const HighRanking: Story = {
  render: () => (
    <ReadingKingLeaderboard
      userRank={{
        rank: 2,
        readCount: 220,
        nextRankDifference: 5,
        progressPercentage: 85,
      }}
    />
  ),
};

export const LowRanking: Story = {
  render: () => (
    <ReadingKingLeaderboard
      userRank={{
        rank: 50,
        readCount: 10,
        nextRankDifference: 30,
        progressPercentage: 20,
      }}
    />
  ),
};