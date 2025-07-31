import path from 'path';
import { tanstackRouter } from '@tanstack/router-plugin/webpack';
import dotenv from 'dotenv';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import webpack from 'webpack';
import 'webpack-dev-server';

dotenv.config();

export default (env, argv) => {
  const config: webpack.Configuration = {
    mode: argv.mode,
    entry: './src/main.tsx',
    output: {
      filename: 'bundle.js',
      path: path.resolve(__dirname, 'dist'),
      publicPath: '/',
    },
    module: {
      rules: [
        {
          test: /\.(ts|tsx)$/,
          use: [
            {
              loader: 'babel-loader',
              options: {
                presets: [
                  '@babel/preset-env',
                  ['@babel/preset-react', { runtime: 'automatic' }],
                  '@babel/preset-typescript',
                ],
              },
            },
          ],
          exclude: /node_modules/,
        },
        {
          test: /\.css$/, // .css 파일을 처리
          use: [
            'style-loader', // CSS를 <style> 태그로 주입
            'css-loader', // CSS를 JavaScript 모듈로 변환
          ],
        },
        {
          test: /\.(png|svg|jpg|jpeg|gif)$/i,
          type: 'asset',
        },
        {
          test: /\.(woff|woff2|eot|ttf|otf)$/i,
          type: 'asset/resource',
          generator: {
            filename: 'assets/[name][ext]',
          },
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
      new HtmlWebpackPlugin({
        template: './index.html', // 템플릿 HTML
        filename: 'index.html', // 출력될 HTML 파일 이름
        inject: true, // <script> 태그 자동 삽입
      }),
      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
        semicolons: true,
      }),
      new webpack.DefinePlugin({
        'process.env': JSON.stringify(process.env),
        'process.env.ENABLE_MSW': JSON.stringify(env.ENABLE_MSW),
      }),
    ],
    devServer: {
      static: [
        { directory: path.join(__dirname, 'dist') },
        { directory: path.join(__dirname, 'public') },
      ],
      port: 3000, // localhost:3000에서 실행
      open: true, // 서버 실행 시 브라우저 자동 열기
      hot: true, // HMR 사용
      historyApiFallback: true, // SPA 라우팅 지원
      client: {
        overlay: true, // 에러 발생 시 브라우저에 띄워줘요
      },
    },
  };

  return config;
};
