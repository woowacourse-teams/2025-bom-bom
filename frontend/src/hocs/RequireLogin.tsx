import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import PageLayout from '@/components/PageLayout/PageLayout';
import RequireLoginCard from '@/components/RequireLoginCard/RequireLoginCard';
import { useWebViewAuth } from '@/libs/webview/useWebViewAuth';
import type { PropsWithChildren } from 'react';

const RequireLogin = ({ children }: PropsWithChildren) => {
  useWebViewAuth();

  const { data: user, isLoading, error } = useQuery(queries.me());

  if (isLoading) {
    return <PageLayout>{/* 로딩 상태는 필요에 따라 추가 */}</PageLayout>;
  }

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
