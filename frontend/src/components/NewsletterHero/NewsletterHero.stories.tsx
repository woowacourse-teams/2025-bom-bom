import type { Meta, StoryObj } from '@storybook/react-webpack5';
import NewsletterHero from './NewsletterHero';

const meta: Meta<typeof NewsletterHero> = {
  title: 'components/NewsletterHero',
  component: NewsletterHero,
  parameters: {
    layout: 'padded',
  },
};
export default meta;

type Story = StoryObj<typeof NewsletterHero>;

export const Default: Story = {
  render: () => <NewsletterHero />,
};