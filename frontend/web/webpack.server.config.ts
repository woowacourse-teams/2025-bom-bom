import path from 'path';
import webpack from 'webpack';
import nodeExternals from 'webpack-node-externals';
import { tanstackRouter } from '@tanstack/router-plugin/webpack';

const config: webpack.Configuration = {
  mode: 'production',
  target: 'node',
  entry: './server/index.ts',
  output: {
    filename: 'server.js',
    path: path.resolve(__dirname, 'dist'),
    clean: false, // 클라이언트 빌드 파일 유지
  },
  externals: [nodeExternals()],
  module: {
    rules: [
      {
        test: /\.(ts|tsx)$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [
                ['@babel/preset-env', { targets: { node: 'current' } }],
                ['@babel/preset-react', { runtime: 'automatic' }],
                '@babel/preset-typescript',
              ],
            },
          },
        ],
        exclude: /node_modules/,
      },
      {
        test: /\.css$/,
        use: ['null-loader'], // 서버에서는 CSS 무시
      },
      {
        test: /\.(avif|jpg|jpeg|gif|png)$/i,
        type: 'asset/resource',
        generator: {
          emit: false, // 서버에서는 이미지 파일 방출하지 않음
        },
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
        generator: {
          emit: false,
        },
      },
      {
        test: /\.svg$/i,
        issuer: /\.[jt]sx?$/,
        use: [{ loader: '@svgr/webpack' }],
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
    alias: {
      '#': path.resolve(__dirname, 'public'),
      '@': path.resolve(__dirname, 'src'),
    },
  },
  plugins: [
    tanstackRouter({
      target: 'react',
      autoCodeSplitting: true,
      semicolons: true,
    }),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('production'),
    }),
  ],
};

export default config;
