import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';

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

    const data = queryClient.getQueryData(queries.userProfile().queryKey);
    if (data) return;

    try {
      const user = await queryClient.fetchQuery(queries.userProfile());

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
  return (
    <PageLayout>
      <Outlet />
    </PageLayout>
  );
}
