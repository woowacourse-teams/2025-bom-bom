const CACHE_NAME = 'bombom-v1';
const urlsToCache = [
  '/',
  '/assets/bombom.png',
  '/assets/arrow-right.svg',
  '/assets/avatar.svg',
  '/assets/book.svg',
  '/assets/bookmark-active.svg',
  '/assets/bookmark-inactive.svg',
  '/assets/category.svg',
  '/assets/check.svg',
  '/assets/chevron-down.svg',
  '/assets/chevron-left.svg',
  '/assets/chevron-right.svg',
  '/assets/chevron-up.svg',
  '/assets/clock.svg',
  '/assets/close.svg',
  '/assets/comment.svg',
  '/assets/compass.svg',
  '/assets/copy.svg',
  '/assets/delete.svg',
  '/assets/edit-off.svg',
  '/assets/edit.svg',
  '/assets/empty-search.svg',
  '/assets/goal.svg',
  '/assets/google.svg',
  '/assets/help.svg',
  '/assets/home.svg',
  '/assets/letter.svg',
  '/assets/link.svg',
  '/assets/lock.svg',
  '/assets/memo.svg',
  '/assets/news.svg',
  '/assets/pet.svg',
  '/assets/postbox.svg',
  '/assets/quick-menu.svg',
  '/assets/reading-glasses.svg',
  '/assets/reading-status.svg',
  '/assets/sparkles.svg',
  '/assets/storage.svg',
  '/assets/streak.svg',
  '/assets/trending-up.svg',
];

// 설치 이벤트 - 캐시에 파일들을 저장
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('Opened cache');
      return cache.addAll(urlsToCache);
    }),
  );
  // 새로운 Service Worker가 설치되면 즉시 활성화
  self.skipWaiting();
});

// 활성화 이벤트 - 오래된 캐시 정리
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        }),
      );
    }),
  );
  // 새로운 Service Worker가 즉시 모든 페이지를 제어하도록 함
  self.clients.claim();
});

// Fetch 이벤트 - Network First 전략 (오프라인 지원)
self.addEventListener('fetch', (event) => {
  // API 요청은 항상 네트워크를 통해 가져오기
  if (event.request.url.includes('/api/')) {
    event.respondWith(
      fetch(event.request).catch(() => {
        // 오프라인 시 기본 에러 응답
        return new Response(
          JSON.stringify({
            error: 'Offline',
            message: '인터넷 연결을 확인해주세요.',
          }),
          {
            status: 503,
            statusText: 'Service Unavailable',
            headers: { 'Content-Type': 'application/json' },
          },
        );
      }),
    );
    return;
  }

  // 정적 자원은 Cache First 전략
  event.respondWith(
    caches.match(event.request).then((response) => {
      // 캐시에 있으면 캐시에서 반환
      if (response) {
        return response;
      }

      // 없으면 네트워크에서 가져오기
      return fetch(event.request)
        .then((response) => {
          // 유효한 응답이 아니면 그대로 반환
          if (
            !response ||
            response.status !== 200 ||
            response.type !== 'basic'
          ) {
            return response;
          }

          // 응답을 복사하여 캐시에 저장
          const responseToCache = response.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });

          return response;
        })
        .catch(() => {
          // 오프라인 시 기본 HTML 페이지 반환 (SPA이므로 index.html로 fallback)
          if (event.request.mode === 'navigate') {
            return caches.match('/');
          }
        });
    }),
  );
});

// 푸시 알림 이벤트 (향후 확장 가능)
self.addEventListener('push', (event) => {
  if (event.data) {
    const data = event.data.json();
    const options = {
      body: data.body || '새로운 알림이 있습니다.',
      icon: '/assets/bombom.png',
      badge: '/assets/bombom.png',
      data: data,
      actions: [
        {
          action: 'view',
          title: '보기',
        },
        {
          action: 'close',
          title: '닫기',
        },
      ],
    };

    event.waitUntil(
      self.registration.showNotification(data.title || '봄봄', options),
    );
  }
});

// 알림 클릭 이벤트
self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  if (event.action === 'view') {
    event.waitUntil(clients.openWindow(event.notification.data.url || '/'));
  }
});
