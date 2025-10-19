/**
 * PM2 Ecosystem Configuration
 * 봄봄 SSR 서버 프로세스 매니저 설정
 */

module.exports = {
  apps: [
    {
      name: 'bombom-ssr',
      script: './dist/server/server.js',
      instances: 2, // CPU 코어 수에 맞게 조정 (또는 'max' 사용)
      exec_mode: 'cluster',
      autorestart: true,
      watch: false,
      max_memory_restart: '1G',

      env: {
        NODE_ENV: 'production',
        PORT: 3000,
      },

      // 로그 설정
      error_file: './logs/error.log',
      out_file: './logs/out.log',
      log_file: './logs/combined.log',
      time: true,
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z',

      // 무중단 배포 설정
      wait_ready: true,
      listen_timeout: 10000,
      kill_timeout: 5000,

      // 재시작 설정
      min_uptime: '10s',
      max_restarts: 10,
      restart_delay: 4000,

      // 성능 모니터링
      instance_var: 'INSTANCE_ID',
      merge_logs: true,

      // 예외 처리
      exp_backoff_restart_delay: 100,
    },
  ],

  // 배포 설정 (옵션)
  deploy: {
    production: {
      user: 'ec2-user',
      host: 'EC2_HOST',
      ref: 'origin/main',
      repo: 'git@github.com:username/repo.git',
      path: '/home/ec2-user/bombom',
      'post-deploy':
        'pnpm install --prod && pm2 reload ecosystem.config.js --env production',
    },
  },
};
