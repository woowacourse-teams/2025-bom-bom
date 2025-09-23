import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { DEFAULT_ERROR_MESSAGES } from '@/apis/constants/defaultErrorMessage';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';
import RequireLoginCard from '@/components/RequireLoginCard/RequireLoginCard';

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
      await queryClient.fetchQuery(queries.me());
    } catch {
      if (isFirstVisit) return redirect({ to: '/recommend' });

      throw new Response(DEFAULT_ERROR_MESSAGES[401], { status: 401 });
    } finally {
      isFirstVisit = false;
    }
  },
  errorComponent: ({ error }) => {
    console.log(error);
    if (error instanceof Response && error.status === 401) {
      return (
        <PageLayout>
          <RequireLoginCard />
        </PageLayout>
      );
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
