import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { useRef } from 'react';
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
    const { queryClient } = context;

    if (location.pathname === '/') {
      isFirstVisit = false;
      return;
    }

    const data = queryClient.getQueryData(queries.me().queryKey);
    if (data) return;

    try {
      const user = await queryClient.fetchQuery(queries.me());

      if (user) {
        window.gtag?.('set', { user_id: user.id });
      }
    } catch {
      if (isFirstVisit) return redirect({ to: '/' });
    } finally {
      isFirstVisit = false;
    }
  },
});

function RouteComponent() {
  const modalRef = useRef<HTMLDivElement>(null);
  const { showModal, handleInstallClick, handleLaterClick, handleCloseModal } =
    useAppInstallPrompt();

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
