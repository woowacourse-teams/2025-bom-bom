import path from 'path';
import compression from 'compression';
import express from 'express';
import { handleSSR } from './render';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(compression());

const distPath = path.join(__dirname, '../static');
const publicPath = path.join(__dirname, '../public');

// 루트 페이지만 SSR 적용 (static보다 먼저!)
app.get('/', handleSSR);

// 정적 파일 제공 (index.html 제외)
app.use(
  express.static(distPath, {
    index: false, // 자동으로 index.html 서빙하지 않음
  }),
);
app.use('/public', express.static(publicPath));

// 나머지 모든 경로는 SPA fallback (CSR로 작동)
app.use((_req, res) => {
  res.sendFile(path.join(distPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT} (SSR enabled for /)`);
});
