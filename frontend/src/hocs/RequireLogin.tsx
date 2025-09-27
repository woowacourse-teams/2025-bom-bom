import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';
import RequireLoginCard from '@/components/RequireLoginCard/RequireLoginCard';
import type { PropsWithChildren } from 'react';

const RequireLogin = ({ children }: PropsWithChildren) => {
  const { data: user, error } = useQuery(queries.me());

  if (error || !user) {
    return (
      <PageLayout>
        <RequireLoginCard />
      </PageLayout>
    );
  }

  return <>{children}</>;
};

export default RequireLogin;
