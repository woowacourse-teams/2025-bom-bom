import { theme } from '@bombom/shared/theme';
import { ThemeProvider } from '@emotion/react';
import Toast from './Toast';
import Button from '../Button/Button';
import { toast } from './utils/toastActions';
import type { ToastPosition } from './Toast.types';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

type StoryArgs = {
  position?: ToastPosition;
  duration?: number;
  limit?: number;
};

const meta = {
  title: 'components/common/Toast',
  component: Toast,
  parameters: {
    layout: 'fullscreen',
  },
  argTypes: {
    position: {
      control: 'select',
      options: [
        'top-left',
        'top-right',
        'top-center',
        'bottom-left',
        'bottom-right',
        'bottom-center',
      ],
    },
    duration: {
      control: { type: 'number', min: 1000, step: 500 },
    },
    limit: {
      control: { type: 'number', min: 1, step: 1 },
    },
  },
  args: {
    position: 'bottom-right',
    duration: 5000,
    limit: 3,
  },
} satisfies Meta<typeof Toast>;

export default meta;
type Story = StoryObj<typeof meta>;

function Controls() {
  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        pointerEvents: 'none', // 뒤 UI 방해하지 않기
      }}
    >
      <div
        style={{
          position: 'absolute',
          left: 24,
          bottom: 24,
          pointerEvents: 'auto', // 버튼은 클릭 가능
          display: 'flex',
          gap: 8,
          flexWrap: 'wrap',
        }}
      >
        <Button
          text={'✅ Success'}
          onClick={() => toast.success('Saved successfully!')}
        />
        <Button
          text={'ℹ️ Info'}
          onClick={() => toast.info('Here is some information.')}
        />

        <Button
          text={'⛔ Error'}
          onClick={() => toast.error('Something went wrong.')}
        />
      </div>
    </div>
  );
}

const Playground = (args: StoryArgs) => {
  return (
    <ThemeProvider theme={theme}>
      <Toast
        position={args.position}
        duration={args.duration}
        limit={args.limit}
      />
      <Controls />
    </ThemeProvider>
  );
};

export const Default: Story = {
  render: (args) => <Playground {...args} />,
};

export const TopLeft: Story = {
  args: { position: 'top-left' },
  render: (args) => <Playground {...args} />,
};

export const BottomCenter: Story = {
  args: { position: 'bottom-center' },
  render: (args) => <Playground {...args} />,
};

export const LongDuration: Story = {
  args: { duration: 8000 },
  render: (args) => <Playground {...args} />,
};

export const LimitOne: Story = {
  args: { limit: 1 },
  render: (args) => <Playground {...args} />,
};
