import { Link } from '@tanstack/react-router';
import React from 'react';
import { sendMessageToRN, isWebView } from '@/libs/webview/webview.utils';
import type { ReactNode } from 'react';

interface ExternalLinkProps {
  to: string;
  children: ReactNode;
}

export const ExternalLink = ({ to, children }: ExternalLinkProps) => {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (!isWebView()) return;

    e.preventDefault();
    sendMessageToRN({
      type: 'OPEN_BROWSER',
      payload: { url: to },
    });
  };

  return (
    <Link to={to} onClick={handleClick}>
      {children}
    </Link>
  );
};
