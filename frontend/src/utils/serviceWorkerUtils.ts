/**
 * 개발 환경에서 기존 PWA 서비스 워커를 해제하는 유틸리티
 */
export const unregisterServiceWorkers = async () => {
  if ('serviceWorker' in navigator) {
    const registrations = await navigator.serviceWorker.getRegistrations();

    for (const registration of registrations) {
      // MSW가 아닌 서비스 워커들을 해제
      if (!registration.scope.includes('mockServiceWorker')) {
        await registration.unregister();
        console.log('Unregistered service worker:', registration.scope);
      }
    }
  }
};

/**
 * MSW와 PWA 서비스 워커 충돌 방지를 위한 초기화
 */
export const initServiceWorkers = async (
  enableMsw: boolean,
  isProduction: boolean,
) => {
  if (enableMsw) {
    // MSW 사용 시 기존 PWA 서비스 워커 해제
    await unregisterServiceWorkers();
    console.log('MSW enabled: PWA service workers unregistered');
  } else if (isProduction && 'serviceWorker' in navigator) {
    // 프로덕션에서 MSW 미사용 시 PWA 서비스 워커 등록
    try {
      const registration = await navigator.serviceWorker.register('/sw.js');
      console.log('PWA Service Worker registered:', registration.scope);
    } catch (error) {
      console.error('PWA Service Worker registration failed:', error);
    }
  }
};
