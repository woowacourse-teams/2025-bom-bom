import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import AppInstallPromptModal from '@/components/AppInstallPromptModal/AppInstallPromptModal';
import PageLayout from '@/components/PageLayout/PageLayout';
import ServerWarningModal from '@/components/ServerWarningModal/ServerWarningModal';
import { useAppInstallPrompt } from '@/hooks/useAppInstallPrompt';
import { useServerWarningModal } from '@/hooks/useServerWarningModal';
import { useWebViewNotificationActive } from '@/libs/webview/useWebViewNotificationActive';
import { useWebViewRegisterToken } from '@/libs/webview/useWebViewRegisterToken';

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
  const {
    showModal,
    handleInstallClick,
    handleLaterClick,
    handleCloseModal,
    modalRef,
  } = useAppInstallPrompt();

  const {
    modalRef: serverModalRef,
    showModal: serverWarningOpen,
    handleCloseModal: handleServerWarningClose,
  } = useServerWarningModal();

  useWebViewRegisterToken();
  useWebViewNotificationActive();

  return (
    <PageLayout>
      {/* 점검 모달 넣기 */}
      <Outlet />
      <ServerWarningModal
        modalRef={serverModalRef}
        isOpen={serverWarningOpen}
        closeModal={handleServerWarningClose}
      />
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
