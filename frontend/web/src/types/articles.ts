import type { components } from './openapi';

export type Article = components['schemas']['ArticleResponse'];
export type NewsletterFilters =
  components['schemas']['ArticleNewsletterStatisticsResponse'];

export type BookmarkFilters =
  components['schemas']['BookmarkNewsletterStatisticsResponse'];

export type HighlightFilters =
  components['schemas']['HighlightStatisticsResponse'];
