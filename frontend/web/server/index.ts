import path from 'path';
import compression from 'compression';
import express from 'express';
import { handleSSR } from './render';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(compression());

const distPath = path.join(__dirname, '../static');
app.use(express.static(distPath));

const publicPath = path.join(__dirname, '../public');
app.use('/public', express.static(publicPath));

// 루트 페이지만 SSR 적용
app.get('/', handleSSR);

  res.sendFile(path.join(distPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`SSR Server running at http://localhost:${PORT}`);
});
