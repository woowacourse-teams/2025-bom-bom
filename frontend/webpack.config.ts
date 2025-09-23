import path from 'path';
import { tanstackRouter } from '@tanstack/router-plugin/webpack';
import CopyWebpackPlugin from 'copy-webpack-plugin';
import dotenv from 'dotenv';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import TerserPlugin from 'terser-webpack-plugin';
import webpack from 'webpack';
import 'webpack-dev-server';

dotenv.config();

export default (env, argv) => {
  const isProduction = argv.mode === 'production';

  const config: webpack.Configuration = {
    mode: argv.mode,
    entry: './src/main.tsx',
    output: {
      filename: 'js/[name].[contenthash:8].js',
      chunkFilename: 'js/[name].[contenthash:8].chunk.js',
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
          test: /\.(png|jpg|jpeg|gif|avif)$/i,
          type: 'asset',
        },
        {
          test: /\.(woff|woff2|eot|ttf|otf)$/i,
          type: 'asset/resource',
          generator: {
            filename: 'assets/[name][ext]',
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
      new HtmlWebpackPlugin({
        template: './index.html', // 템플릿 HTML
        filename: 'index.html', // 출력될 HTML 파일 이름
        inject: true, // <script> 태그 자동 삽입
        favicon: './public/assets/png/logo.png',
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
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'public/assets',
            to: 'assets',
          },
        ],
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
    optimization: {
      usedExports: true, // 사용되지 않는 export 제거
      sideEffects: false, // side effect가 없는 모듈들 최적화
      minimize: isProduction,
      minimizer: isProduction
        ? [
            new TerserPlugin({
              terserOptions: {
                compress: {
                  drop_console: true, // console.log 제거
                  drop_debugger: true, // debugger 제거
                  pure_funcs: ['console.log', 'console.info', 'console.warn'], // console.log 제거
                },
                mangle: {
                  toplevel: true,
                  reserved: [], // 변수 이름 변경 방지
                },
                format: {
                  comments: false, // 주석 제거
                },
              },
              extractComments: false, // 주석 제거
            }),
          ]
        : [],
      splitChunks: {
        chunks: 'all', // 모든 청크에 대해 코드 분할 적용
        minSize: 30000, // 최소 분할 크기 (30KB)
        maxSize: 100000, // 최대 분할 크기 (50KB)
        minChunks: 1, // 최소 참조 횟수 (1회 이상 사용된 모듈 분리)
        maxAsyncRequests: 10, // 비동기 요청 최대 수
        maxInitialRequests: 5, // 초기 요청 최대 수
        automaticNameDelimiter: '~', // 자동 생성 청크 이름 구분자
        cacheGroups: {
          // React 관련 라이브러리 별도 분리
          react: {
            test: /[\\/]node_modules[\\/](react|react-dom)[\\/]/,
            name: 'react',
            chunks: 'all',
            priority: 30,
          },
          // vendor 라이브러리 별도 분리
          vendor: {
            test: /[\\/]node_modules[\\/](?!(react|react-dom)[\\/])/,
            name: 'vendor',
            chunks: 'all',
            priority: 20,
            minSize: 50000,
          },
          // 공통 모듈 분리
          common: {
            name: 'common',
            minChunks: 3, // 3번 이상 사용된 모듈
            chunks: 'all',
            priority: 10,
            reuseExistingChunk: true,
          },
        },
      },
    },
  };

  return config;
};
