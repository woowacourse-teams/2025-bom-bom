import path from 'path';
import compression from 'compression';
import express from 'express';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(compression());

const distPath = path.join(__dirname, '../static');
app.use(express.static(distPath));

const publicPath = path.join(__dirname, '../public');
app.use('/public', express.static(publicPath));

// const ssrRoutes = ['/'];

app.use((req, res) => {
  res.sendFile(path.join(distPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`SSR Server running at http://localhost:${PORT}`);
});
