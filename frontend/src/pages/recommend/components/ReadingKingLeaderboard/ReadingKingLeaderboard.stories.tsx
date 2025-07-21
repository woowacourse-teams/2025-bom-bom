import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ReadingKingLeaderboard from './ReadingKingLeaderboard';

const meta: Meta<typeof ReadingKingLeaderboard> = {
  title: 'components/bombom/ReadingKingLeaderboard',
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
