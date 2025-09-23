import { createFileRoute } from '@tanstack/react-router';
import MaintenanceCard from '@/pages/system/components/MaintenanceCard';

export const Route = createFileRoute('/maintenance')({
  component: () => RouteComponent,
});

function RouteComponent() {
  return <MaintenanceCard />;
}
