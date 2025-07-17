import type { Meta, StoryObj } from '@storybook/react-webpack5';
import NewsletterHero from './NewsletterHero';

const meta: Meta<typeof NewsletterHero> = {
  title: 'components/Bom-Bom/NewsletterHero',
  component: NewsletterHero,
};
export default meta;

type Story = StoryObj<typeof NewsletterHero>;

export const Default: Story = {};
