import Pagination from './Pagination';
import type { Meta, StoryObj } from '@storybook/react-webpack5';

const meta: Meta<typeof Pagination> = {
  title: 'Components/common/Pagination',
  component: Pagination,
  parameters: {
    layout: 'centered',
  },
  argTypes: {
    currentPage: {
      control: { type: 'number', min: 0 },
      description: '현재 페이지 번호 (0부터 시작)',
    },
    totalPages: {
      control: { type: 'number', min: 1 },
      description: '총 페이지 수',
    },
    onPageChange: {
      action: 'page-changed',
      description: '페이지 변경 시 호출되는 콜백 함수',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

// 기본 스토리 - 작은 페이지 수
export const Default: Story = {
  args: {
    currentPage: 0,
    totalPages: 5,
  },
};

// 첫 번째 페이지
export const FirstPage: Story = {
  args: {
    currentPage: 0,
    totalPages: 10,
  },
};

// 마지막 페이지
export const LastPage: Story = {
  args: {
    currentPage: 9,
    totalPages: 10,
  },
};

// 중간 페이지
export const MiddlePage: Story = {
  args: {
    currentPage: 5,
    totalPages: 10,
  },
};

// 많은 페이지 수 (첫 번째 근처)
export const ManyPagesFirst: Story = {
  args: {
    currentPage: 1,
    totalPages: 20,
  },
};

// 많은 페이지 수 (마지막 근처)
export const ManyPagesAlmostLast: Story = {
  args: {
    currentPage: 18,
    totalPages: 20,
  },
};

export const ManyPagesLast: Story = {
  args: {
    currentPage: 20,
    totalPages: 20,
  },
};

// 많은 페이지 수 (중간)
export const ManyPagesMiddle: Story = {
  args: {
    currentPage: 10,
    totalPages: 20,
  },
};
