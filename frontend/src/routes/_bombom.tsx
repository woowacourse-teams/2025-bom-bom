import { createFileRoute, Outlet } from '@tanstack/react-router';
import { DEFAULT_ERROR_MESSAGES } from '@/apis/constants/defaultErrorMessage';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';
import RequireLoginCard from '@/components/RequireLoginCard/RequireLoginCard';

export const Route = createFileRoute('/_bombom')({
  component: RouteComponent,
  beforeLoad: async ({ context, location }) => {
    const { queryClient } = context;

    try {
      await queryClient.fetchQuery(queries.me());
    } catch {
      if (location.pathname !== '/recommend') {
        throw new Response(DEFAULT_ERROR_MESSAGES[401], { status: 401 });
      }
    }
  },
  errorComponent: ({ error }) => {
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
