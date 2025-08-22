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
