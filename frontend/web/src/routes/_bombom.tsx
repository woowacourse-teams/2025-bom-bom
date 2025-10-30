import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import AppInstallPromptModal from '@/components/AppInstallPromptModal/AppInstallPromptModal';
import PageLayout from '@/components/PageLayout/PageLayout';
import { useAppInstallPrompt } from '@/hooks/useAppInstallPrompt';

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
