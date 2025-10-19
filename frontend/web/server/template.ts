interface TemplateParams {
  html: string;
  css: string;
  scripts: string[];
  styles: string[];
  title?: string;
  description?: string;
  ogImage?: string;
  ogUrl?: string;
}

export function htmlTemplate({
  html,
  css,
  scripts,
  styles,
  title = '봄봄',
  description = '봄봄 - 뉴스레터, 아티클, 트렌드 정보를 한 곳에!',
  ogImage = '/public/assets/png/logo.png',
  ogUrl = 'https://www.bombom.news',
}: TemplateParams): string {
  return `<!doctype html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta
      name="viewport"
      content="initial-scale=1.0; maximum-scale=1.0; minimum-scale=1.0; user-scalable=no;"
    />
    <title>${title}</title>
    <meta name="description" content="${description}" />
    <meta name="keywords" content="봄봄, 뉴스레터, 아티클, 트렌드, 정보" />
    <meta name="author" content="봄봄팀" />
    <meta name="theme-color" content="#FF9966" />
    <link rel="icon" type="image/png" href="/public/assets/png/logo.png" />
    <link rel="canonical" href="${ogUrl}" />

    <!-- Open Graph / Facebook -->
    <meta property="og:type" content="website" />
    <meta property="og:title" content="${title}" />
    <meta property="og:description" content="${description}" />
    <meta property="og:image" content="${ogImage}" />
    <meta property="og:image:width" content="512" />
    <meta property="og:image:height" content="512" />
    <meta property="og:image:alt" content="봄봄 로고" />
    <meta property="og:url" content="${ogUrl}" />
    <meta property="og:site_name" content="봄봄" />
    <meta property="og:locale" content="ko_KR" />

    <!-- Twitter -->
    <meta name="twitter:card" content="summary_large_image" />
    <meta name="twitter:title" content="${title}" />
    <meta name="twitter:description" content="${description}" />
    <meta name="twitter:image" content="${ogImage}" />
    <meta name="twitter:image:alt" content="봄봄 로고" />
    <meta
      name="naver-site-verification"
      content="972790f41cbb91a92f444d39677fa993b3f6accc"
    />

    ${styles.map((href) => `<link rel="stylesheet" href="${href}" />`).join('\n    ')}
    ${css}
  </head>
  <body>
    <div id="root">${html}</div>
    ${scripts.map((src) => `<script src="${src}" defer></script>`).join('\n    ')}
  </body>
</html>`;
}
