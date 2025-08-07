declare global {
  interface Window {
    dataLayer: unknown[];
    gtag?: (...args: unknown[]) => void;
  }
}

export const initGA = (measurementId: string) => {
  if (!measurementId) {
    console.warn('[GA] Measurement ID missing');
    return;
  }

  // gtag.js 삽입
  const script = document.createElement('script');
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${measurementId}`;
  document.head.appendChild(script);

  // gtag 초기화
  window.dataLayer = window.dataLayer || [];
  function gtag(...args: unknown[]) {
    window.dataLayer.push(args);
  }
  window.gtag = gtag;

  gtag('js', new Date());
  gtag('config', measurementId, {
    send_page_view: false, // 수동으로 pageview 보내기 위해 false로 설정
  });
};
