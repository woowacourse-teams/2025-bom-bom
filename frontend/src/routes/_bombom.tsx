import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';

let isFirstVisit = true;

export const Route = createFileRoute('/_bombom')({
  component: RouteComponent,
  beforeLoad: async ({ context, location }) => {
    const { queryClient } = context;

    if (location.pathname === '/recommend') {
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
      if (isFirstVisit) return redirect({ to: '/recommend' });
    } finally {
      isFirstVisit = false;
    }
  },
});

function RouteComponent() {
  useWebViewAuth();

  return (
    <PageLayout>
      <Outlet />
    </PageLayout>
  );
}
