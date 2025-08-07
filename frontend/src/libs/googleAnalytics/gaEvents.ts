import ReactGA from 'react-ga4';

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
  ReactGA.event({
    category,
    action,
    label,
    value,
  });
};
