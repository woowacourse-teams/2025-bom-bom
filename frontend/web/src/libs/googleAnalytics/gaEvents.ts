import { logger } from '@bombom/shared/utils';

interface TrackEventParams {
  category: string;
  action: string;
  label?: string;
  value?: number;
}

export const trackEvent = ({
  category,
  action,
  label,
  value,
}: TrackEventParams) => {
  if (typeof window.gtag !== 'function') {
    logger.warn('[GA] gtag is not initialized');
    return;
  }

  window.gtag('event', action, {
    event_category: category,
    event_label: label,
    value,
  });
};
