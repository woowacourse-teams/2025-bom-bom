declare global {
  interface Window {
    dataLayer: unknown[];
    gtag?: (...args: unknown[]) => void;
  }
}

export const initGA = (
  measurementId: string,
  gtagUrl: string = 'https://www.googletagmanager.com/gtag/js',
) => {
  if (!measurementId) {
    console.warn('[GA] Measurement ID missing');
    return;
  }

  // gtag.js 삽입
  const script = document.createElement('script');
  script.async = true;
  script.src = `${gtagUrl}?id=${measurementId}`;
  document.body.appendChild(script);

  // gtag 초기화
  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag() {
    // eslint-disable-next-line prefer-rest-params
    window.dataLayer.push(arguments);
  };

  window.gtag('js', new Date());
  window.gtag('config', measurementId);
};
