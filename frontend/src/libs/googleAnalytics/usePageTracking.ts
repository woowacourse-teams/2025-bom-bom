import { useLocation } from '@tanstack/react-router';
import { useEffect } from 'react';
import ReactGA from 'react-ga4';

export const usePageTracking = () => {
  const location = useLocation();

  useEffect(() => {
    ReactGA.send({
      hitType: 'pageview',
      page: location.pathname + location.search,
      title: document.title,
    });
  }, [location.pathname, location.search]);
};
