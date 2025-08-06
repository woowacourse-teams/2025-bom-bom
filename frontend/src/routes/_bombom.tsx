import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';

export const Route = createFileRoute('/_bombom')({
  component: RouteComponent,
  beforeLoad: async ({ context, location }) => {
    const { queryClient } = context;

    try {
      await queryClient.fetchQuery(queries.me());
    } catch {
      if (location.pathname !== '/recommend') {
        alert('이 기능을 이용하려면 로그인이 필요합니다.');
        return redirect({ to: '/login' });
      }
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
