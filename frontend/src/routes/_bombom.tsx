import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';

let isFirstCheck = true;

export const Route = createFileRoute('/_bombom')({
  component: RouteComponent,
  beforeLoad: async ({ context, location }) => {
    const { queryClient } = context;

    if (!isFirstCheck) {
      return;
    }

    try {
      await queryClient.fetchQuery(queries.me());
    } catch {
      if (location.pathname !== '/recommend') {
        throw redirect({ to: '/recommend' });
      }
    } finally {
      isFirstCheck = false;
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
