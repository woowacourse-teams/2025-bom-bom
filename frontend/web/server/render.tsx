import fs from 'fs';
import path from 'path';
import { htmlTemplate } from './template';
import { render } from '../src/entry-server';
import type { Request, Response } from 'express';

// 빌드된 assets 매니페스트 (webpack에서 생성)
let manifest: Record<string, string> = {};

try {
  const manifestPath = path.join(__dirname, '../dist/asset-manifest.json');
  if (fs.existsSync(manifestPath)) {
    manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
  }
} catch (error) {
  console.error('Failed to load asset manifest:', error);
}

export async function handleSSR(req: Request, res: Response) {
  try {
    const url = req.originalUrl;

    // SSR 렌더링
    const { html, css } = await render(url);

    // 빌드된 JavaScript와 CSS 파일 찾기
    const scripts: string[] = [];
    const styles: string[] = [];

    // manifest에서 main 파일들 추출
    Object.entries(manifest).forEach(([key, value]) => {
      if (key.endsWith('.js')) {
        scripts.push(value);
      } else if (key.endsWith('.css')) {
        styles.push(value);
      }
    });

    // manifest가 없으면 기본 경로 사용
    if (scripts.length === 0) {
      scripts.push('/js/main.js');
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
