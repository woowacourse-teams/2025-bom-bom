import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { StrictMode } from 'react';
import reset from './styles/reset.ts';
import { Global } from '@emotion/react';

async function enableMocking() {
  const { worker } = await import('./mocks/browser');

  return worker.start({
    onUnhandledRequest: 'bypass',
  });
}

enableMocking().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <Global styles={reset} />
      <App />
    </StrictMode>,
  );
});
