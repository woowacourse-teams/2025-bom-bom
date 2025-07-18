import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/recommend')({
  component: RouteComponent,
});

function RouteComponent() {
  return <div>Hello recommend!</div>;
}
