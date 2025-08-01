import { useTheme } from '@emotion/react';
import Carousel from './Carousel';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/common/Carousel',
  component: Carousel,
  parameters: {
    layout: 'centered',
  },
  args: {
    children: [],
  },
} satisfies Meta<typeof Carousel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => {
    const theme = useTheme();

    return (
      <Carousel>
        <div
          style={{
            width: '100%',
            height: '100%',
            padding: '54px',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: theme.colors.primary,
            font: theme.fonts.heading2,
          }}
        >
          슬라이드1
        </div>

        <div
          style={{
            width: '100%',
            height: '100%',
            padding: '54px',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: theme.colors.primaryLight,
            font: theme.fonts.heading2,
          }}
        >
          슬라이드2
        </div>
      </Carousel>
    );
  },
};
