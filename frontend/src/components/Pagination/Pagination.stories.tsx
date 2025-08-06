import { useState } from 'react';
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
  render: (args) => {
    const [currentPage, setCurrentPage] = useState(args.currentPage);

    return (
      <Pagination
        {...args}
        currentPage={currentPage}
        onPageChange={setCurrentPage}
      />
    );
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    currentPage: 1,
    totalPages: 7,
  },
};

export const SmallPages: Story = {
  args: {
    currentPage: 1,
    totalPages: 3,
  },
};

export const BigPages: Story = {
  args: {
    currentPage: 16,
    totalPages: 20,
  },
};
