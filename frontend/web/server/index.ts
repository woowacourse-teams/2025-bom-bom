import path from 'path';
import express from 'express';
import { handleSSR } from './render';

const app = express();
const PORT = process.env.PORT || 3000;

// app.use(compression());

const staticPath = path.join(__dirname, '../static');
const publicPath = path.join(__dirname, '../public');

app.get('/', handleSSR);

app.use(
  express.static(staticPath, {
    index: false,
  }),
);
app.use('/public', express.static(publicPath));

app.use((_req, res) => {
  res.sendFile(path.join(staticPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT} (SSR enabled for /)`);
});
