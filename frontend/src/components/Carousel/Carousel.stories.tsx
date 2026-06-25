import { useTheme } from '@emotion/react';
import Carousel from './Carousel';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta = {
  title: 'components/common/Carousel',
  component: Carousel,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof Carousel>;

export default meta;
type Story = StoryObj<typeof meta>;

interface SlideType {
  label: string;
  backgroundColor: 'primary' | 'primaryLight';
}
interface SlideBoxProps {
  backgroundColor: 'primary' | 'primaryLight';
  children: React.ReactNode;
}

const slides: SlideType[] = [
  { label: '슬라이드1', backgroundColor: 'primary' },
  { label: '슬라이드2', backgroundColor: 'primaryLight' },
];

const SlideBox = ({ backgroundColor, children }: SlideBoxProps) => {
  const theme = useTheme();

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        padding: '52px',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: theme.colors[backgroundColor],
        font: theme.fonts.heading2,
      }}
    >
      {children}
    </div>
  );
};

export const DefaultTimer: Story = {
  args: {
    timer: true,
    children: [],
  },
  render: (args) => (
    <Carousel timer={args.timer}>
      {slides.map(({ label, backgroundColor }) => (
        <SlideBox key={label} backgroundColor={backgroundColor}>
          {label}
        </SlideBox>
      ))}
    </Carousel>
  ),
};

export const CustomTimer: Story = {
  args: {
    timer: 1000,
    children: [],
  },

  render: ({ timer }) => (
    <Carousel timer={timer}>
      {slides.map(({ label, backgroundColor }) => (
        <SlideBox key={label} backgroundColor={backgroundColor}>
          {label}
        </SlideBox>
      ))}
    </Carousel>
  ),
};

export const UserControlled: Story = {
  args: {
    timer: false,
    children: [],
  },

  render: ({ timer }) => (
    <Carousel timer={timer}>
      {slides.map(({ label, backgroundColor }) => (
        <SlideBox key={label} backgroundColor={backgroundColor}>
          {label}
        </SlideBox>
      ))}
    </Carousel>
  ),
};
