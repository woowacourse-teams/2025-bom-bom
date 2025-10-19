import path from 'path';
// import compression from 'compression';
import express from 'express';
import { handleSSR } from './render';

const app = express();
const PORT = process.env.PORT || 3000;

// 정적 파일 제공
const distPath = path.join(__dirname, '../static');
app.use('/static', express.static(distPath, { index: false }));

// public 폴더 제공
const publicPath = path.join(__dirname, '../public');
app.use('/public', express.static(publicPath));

// SSR이 필요한 경로들
const ssrRoutes = ['/'];

// SSR 라우트 핸들러
ssrRoutes.forEach((route) => {
  app.get(route, handleSSR);
});

// 나머지 모든 경로는 SPA fallback (index.html 제공)
// 이렇게 하면 /my, /bookmark 등 다른 페이지는 CSR로 작동
app.get('*', (req, res) => {
  res.sendFile(path.join(distPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`SSR Server running at http://localhost:${PORT}`);
});
