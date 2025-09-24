import type { Article } from './articles';

export type LocalGuideMail = Article & {
  createdAt: string;
};
