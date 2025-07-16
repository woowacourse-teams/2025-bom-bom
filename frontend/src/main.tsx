import { createRoot } from 'react-dom/client';
import App from './App.tsx';

async function enableMocking() {
  const { worker } = await import('./mocks/browser');

  return worker.start({
    onUnhandledRequest: 'bypass',
  });
}

enableMocking().then(() => {
  createRoot(document.getElementById('root')!).render(<App />);
});
