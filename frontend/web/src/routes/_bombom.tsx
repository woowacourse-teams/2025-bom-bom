import { useQueryClient } from '@tanstack/react-query';
import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { useEffect } from 'react';
import { queries } from '@/apis/queries';
import AppInstallPromptModal from '@/components/AppInstallPromptModal/AppInstallPromptModal';
import PageLayout from '@/components/PageLayout/PageLayout';
import { useAppInstallPrompt } from '@/hooks/useAppInstallPrompt';
import { useWebViewNotificationActive } from '@/libs/webview/useWebViewNotificationActive';
import { sendMessageToRN } from '@/libs/webview/webview.utils';
import { isWebView } from '@/utils/device';

let isFirstVisit = true;

export const Route = createFileRoute('/_bombom')({
  component: RouteComponent,
  beforeLoad: async ({
    context,
    location,
  }): Promise<void | ReturnType<typeof redirect>> => {
    if (!isFirstVisit) return;

    const { queryClient } = context;

    const data = queryClient.getQueryData(queries.userProfile().queryKey);
    if (data) {
      isFirstVisit = false;
      return;
    }

    try {
      const user = await queryClient.fetchQuery(queries.userProfile());
      if (user) {
        window.gtag?.('set', { user_id: user.id });
      }
    } catch {
      if (isFirstVisit && location.pathname !== '/')
        return redirect({ to: '/' });
    } finally {
      isFirstVisit = false;
    }
  },
});

function RouteComponent() {
  const queryClient = useQueryClient();
  const {
    showModal,
    handleInstallClick,
    handleLaterClick,
    handleCloseModal,
    modalRef,
  } = useAppInstallPrompt();
  useWebViewNotificationActive();

  useEffect(() => {
    if (!isWebView()) return;

    const data = queryClient.getQueryData(queries.userProfile().queryKey);
    const isLoggedIn = data && data.id;

    if (isLoggedIn) {
      sendMessageToRN({
        type: 'REGISTER_FCM_TOKEN_LOGGED_IN',
        payload: {
          memberId: data.id,
        },
      });
    }
  }, []);

  return (
    <PageLayout>
      <Outlet />
      <AppInstallPromptModal
        modalRef={modalRef}
        isOpen={showModal}
        closeModal={handleCloseModal}
        onInstallClick={handleInstallClick}
        onLaterClick={handleLaterClick}
      />
    </PageLayout>
  );
}
