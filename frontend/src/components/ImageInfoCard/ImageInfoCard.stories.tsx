import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ImageInfoCard from './ImageInfoCard';

const meta: Meta<typeof ImageInfoCard> = {
  title: 'components/common/ImageInfoCard',
  component: ImageInfoCard,
};
export default meta;

type Story = StoryObj<typeof ImageInfoCard>;

export const Default: Story = {
  args: {
    imageUrl:
      'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
    title: '타이틀',
    description:
      '시간이 없어도 세상을 궁금하니까! 세상 돌아가는 소식을 쉽고 재밌게 알아보세요',
  },
};

export const NoDescription: Story = {
  args: {
    imageUrl:
      'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
    title: '타이틀',
  },
};
