import fs from 'fs';
import path from 'path';
import { htmlTemplate } from './template';
import { render } from '../src/entry-server';
import type { Request, Response } from 'express';

export async function handleSSR(req: Request, res: Response) {
  try {
    const url = req.originalUrl;

    // SSR 렌더링
    const { html, css } = await render(url);

    // 빌드된 index.html에서 스크립트 목록 추출
    const scripts: string[] = [];
    const styles: string[] = [];

    try {
      const indexPath = path.join(__dirname, '../static/index.html');
      if (fs.existsSync(indexPath)) {
        const indexHtml = fs.readFileSync(indexPath, 'utf-8');

        // <script> 태그에서 src 추출
        const scriptRegex = /<script[^>]+src="([^"]+)"[^>]*><\/script>/g;
        let match: RegExpExecArray | null;
        while ((match = scriptRegex.exec(indexHtml)) !== null) {
          scripts.push(match[1]);
        }

        // <link rel="stylesheet"> 태그에서 href 추출
        const linkRegex =
          /<link[^>]+rel="stylesheet"[^>]+href="([^"]+)"[^>]*>/g;
        while ((match = linkRegex.exec(indexHtml)) !== null) {
          styles.push(match[1]);
        }
      }
    } catch (error) {
      console.error('Failed to read index.html:', error);
    }

    // 메타 정보 (라우트 기반으로 동적 생성 가능)
    const metaInfo = {
      title: '봄봄 | 뉴스레터 추천',
      description: '봄봄 - 뉴스레터, 아티클, 트렌드 정보를 한 곳에!',
      ogImage: '/public/assets/png/logo.png',
      ogUrl: `https://www.bombom.news${url}`,
    };

    const fullHtml = htmlTemplate({
      html,
      css,
      scripts,
      styles,
      ...metaInfo,
    });

    res.status(200).set({ 'Content-Type': 'text/html' }).end(fullHtml);
  } catch (error) {
    console.error('SSR error:', error);
    res.status(500).send('Internal Server Error');
  }
}
