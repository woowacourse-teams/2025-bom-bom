import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync } from 'node:fs';
import http from 'node:http';
import https from 'node:https';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import readline from 'node:readline';
import { createRequire } from 'node:module';

const frontendRoot = path.resolve(process.env.BOMBOM_FRONTEND_ROOT ?? '../../frontend');
const require = createRequire(path.join(frontendRoot, 'package.json'));
const { chromium, firefox, webkit } = require('@playwright/test');
const webpack = require('webpack');

// JUnit이 실제 테스트 백엔드를 시작한 뒤 실행한다. 외부 서비스에는 접속하지 않는다.
const backendPort = Number(process.argv[2]);
const engineName = process.argv[3] ?? 'chromium';
const engine = { chromium, firefox, webkit }[engineName];
assert.ok(engine, 'Supported browser engine is required');
const directory = mkdtempSync(
  path.join(os.tmpdir(), 'bombom-browser-security-'),
);
const requests = [];
let runtimeBundle;
let idpOutcome = 'cancel';
let browser;
let proxy;
let result;
let tunnel;
const tunnelSockets = new Set();
try {
  execFileSync(
    'openssl',
    [
      'req',
      '-x509',
      '-newkey',
      'rsa:2048',
      '-nodes',
      '-keyout',
      path.join(directory, 'key.pem'),
      '-out',
      path.join(directory, 'cert.pem'),
      '-days',
      '1',
      '-subj',
      '/CN=bombom.test',
    ],
    { stdio: 'ignore' },
  );
  proxy = https.createServer(
    {
      key: readFileSync(path.join(directory, 'key.pem')),
      cert: readFileSync(path.join(directory, 'cert.pem')),
    },
    (request, response) => {
      if (request.headers.host?.startsWith('attacker.bombom.test:')) {
        response.writeHead(200, {
          'Content-Type': 'text/html',
          'Set-Cookie':
            '__Host-BOMBOM_APPLE_AUTH=' +
            'A'.repeat(43) +
            '; Domain=bombom.test; Path=/; Secure; HttpOnly; SameSite=None',
        });
        response.end('<!doctype html><title>Cookie injection fixture</title>');
        return;
      }
      if (request.headers.host?.startsWith('idp.example.test:')) {
        const state = new URL(request.url, idpUrl).searchParams.get('state');
        if (!state) {
          response.writeHead(400);
          response.end();
          return;
        }
        const nonce = new URL(request.url, idpUrl).searchParams.get('nonce');
        const resultInput =
          idpOutcome === 'cancel'
            ? '<input name="error" value="access_denied">'
            : `<input name="code" value="${nonce}">`;
        const escapedState = state
          .replaceAll('&', '&amp;')
          .replaceAll('"', '&quot;')
          .replaceAll('<', '&lt;');
        response.writeHead(200, { 'Content-Type': 'text/html' });
        response.end(`<!doctype html><form method="POST" action="${apiUrl}/login/sso/verify/success/apple">
          <input name="state" value="${escapedState}">${resultInput}</form>
          <script>setTimeout(() => document.forms[0].submit(), 100)</script>`);
        return;
      }
      if (
        request.headers.host?.startsWith('app.bombom.test:') &&
        request.url === '/security-runtime.js'
      ) {
        response.writeHead(200, { 'Content-Type': 'application/javascript' });
        response.end(runtimeBundle);
        return;
      }
      if (!request.headers.host?.startsWith('api.bombom.test:')) {
        response.writeHead(200, { 'Content-Type': 'text/html' });
        const script = request.headers.host?.startsWith('app.bombom.test:')
          ? '<script src="/security-runtime.js"></script>'
          : '';
        response.end(
          '<!doctype html><title>Security browser fixture</title>' + script,
        );
        return;
      }
      requests.push({
        path: request.url,
        method: request.method,
        cookies: (request.headers.cookie ?? '')
          .split(';')
          .map((value) => value.trim().split('=')[0]),
        origin: request.headers.origin,
        csrfHeader: Boolean(request.headers['x-csrf-token']),
      });
      const upstream = http.request(
        {
          hostname: '127.0.0.1',
          port: backendPort,
          path: request.url,
          method: request.method,
          headers: { ...request.headers, 'x-forwarded-proto': 'https' },
        },
        (upstreamResponse) => {
          const headers = { ...upstreamResponse.headers };
          if (headers.location?.startsWith('http://localhost:3000')) {
            headers.location = headers.location.replace(
              'http://localhost:3000',
              appUrl,
            );
          }
          if (headers.location?.startsWith('https://appleid.apple.com/')) {
            const target = new URL(headers.location);
            headers.location = idpUrl + target.pathname + target.search;
          }
          response.writeHead(upstreamResponse.statusCode, headers);
          upstreamResponse.pipe(response);
        },
      );
      upstream.on('error', () => {
        response.writeHead(502);
        response.end();
      });
      request.pipe(upstream);
    },
  );
  await new Promise((resolve) => proxy.listen(0, '127.0.0.1', resolve));
  const port = proxy.address().port;
  const appUrl = `https://app.bombom.test:${port}`;
  const apiUrl = `https://api.bombom.test:${port}`;
  const evilUrl = `https://evil.example.test:${port}`;
  const idpUrl = `https://idp.example.test:${port}`;
  // 실제 프론트 공통 요청 모듈을 빌드해 브라우저에서 실행한다(React UI는 포함하지 않는다).
  await new Promise((resolve, reject) => {
    const compiler = webpack({
      context: frontendRoot,
      mode: 'development',
      devtool: false,
      entry: path.join(frontendRoot, 'src/apis/fetcher.ts'),
      output: {
        path: directory,
        filename: 'security-runtime.js',
        library: { name: 'bombomApi', type: 'window' },
      },
      resolve: {
        extensions: ['.ts', '.js'],
        alias: { '@': path.join(frontendRoot, 'src') },
      },
      module: {
        rules: [
          {
            test: /\.ts$/,
            use: {
              loader: require.resolve('babel-loader'),
              options: {
                babelrc: false,
                configFile: false,
                presets: [require.resolve('@babel/preset-typescript')],
              },
            },
          },
        ],
      },
      plugins: [
        new webpack.DefinePlugin({
          'process.env': JSON.stringify({
            API_BASE_URL: apiUrl + '/api/v1',
            API_TOKEN: 'test',
            NODE_ENV: 'development',
          }),
        }),
      ],
    });
    compiler.run((error, stats) =>
      compiler.close((closeError) => {
        if (error || closeError || stats?.hasErrors())
          reject(
            error ??
              closeError ??
              new Error(stats.toString({ all: false, errors: true })),
          );
        else {
          runtimeBundle = readFileSync(
            path.join(directory, 'security-runtime.js'),
          );
          resolve();
        }
      }),
    );
  });
  // CONNECT는 테스트 도메인만 로컬 HTTPS 서버로 연결한다. 시스템 DNS/hosts는 변경하지 않는다.
  tunnel = http.createServer();
  tunnel.on('connect', (request, socket, head) => {
    const target = new URL('http://' + request.url);
    if (
      ![
        'app.bombom.test',
        'api.bombom.test',
        'evil.example.test',
        'idp.example.test',
        'attacker.bombom.test',
      ].includes(target.hostname) ||
      Number(target.port) !== port
    ) {
      socket.end('HTTP/1.1 403 Forbidden\r\n\r\n');
      return;
    }
    tunnelSockets.add(socket);
    const upstream = net.connect(port, '127.0.0.1', () => {
      socket.write('HTTP/1.1 200 Connection Established\r\n\r\n');
      if (head.length) upstream.write(head);
      upstream.pipe(socket);
      socket.pipe(upstream);
    });
    tunnelSockets.add(upstream);
    socket.on('close', () => {
      tunnelSockets.delete(socket);
      upstream.destroy();
    });
    upstream.on('close', () => {
      tunnelSockets.delete(upstream);
      socket.destroy();
    });
    socket.on('error', () => upstream.destroy());
    upstream.on('error', () => socket.destroy());
  });
  await new Promise((resolve) => tunnel.listen(0, '127.0.0.1', resolve));
  process.stdout.write(JSON.stringify({ ready: true, port }) + '\n');
  const lines = readline.createInterface({ input: process.stdin });
  await new Promise((resolve) => lines.once('line', resolve));
  lines.close();
  process.stdin.pause();
  browser = await engine.launch({
    timeout: 15000,
    headless: true,
    proxy: { server: `http://127.0.0.1:${tunnel.address().port}` },
    ...(engineName === 'firefox'
      ? { firefoxUserPrefs: { 'network.cookie.cookieBehavior': 0 } }
      : {}),
    ...(engineName === 'chromium' && !existsSync(engine.executablePath())
      ? {
          executablePath:
            '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
        }
      : {}),
  });
  const context = await browser.newContext({ ignoreHTTPSErrors: true });
  await context.route('**/*', (route) => {
    const host = new URL(route.request().url()).hostname;
    return [
      'app.bombom.test',
      'api.bombom.test',
      'evil.example.test',
      'idp.example.test',
      'attacker.bombom.test',
    ].includes(host)
      ? route.continue()
      : route.abort();
  });
  const page = await context.newPage();
  await page.goto(apiUrl + '/__test/security/login');
  await page.goto(appUrl);
  const member = await page.evaluate(async (url) => {
    const response = await fetch(url + '/api/v1/members/me', {
      credentials: 'include',
    });
    return { status: response.status, body: await response.json() };
  }, apiUrl);
  assert.equal(
    member.status,
    200,
    'same-site frontend can read authenticated member',
  );
  assert.equal(member.body.email, 'acceptance@bombom.news');
  const mainCookie = (await context.cookies(apiUrl)).find(
    (cookie) => cookie.name === 'JSESSIONID_BROWSER_TEST',
  );
  assert.equal(mainCookie?.sameSite, 'Lax');
  assert.equal(mainCookie?.httpOnly, true);
  assert.equal(mainCookie?.secure, true);

  const mutation = await page.evaluate(async (url) => {
    const change = (token) =>
      fetch(url + '/api/v1/members/me', {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'X-CSRF-TOKEN': token } : {}),
        },
        body: JSON.stringify({ nickname: 'invalid..nickname' }),
      }).then((response) => response.status);
    const missing = await change();
    const csrf = await fetch(url + '/api/v1/auth/csrf', {
      credentials: 'include',
    }).then((response) => response.json());
    return {
      missing,
      valid: await change(csrf.token),
      invalid: await change('invalid-token'),
    };
  }, apiUrl);
  assert.deepEqual(mutation, { missing: 403, valid: 400, invalid: 403 });
  const beforeRuntime = requests.length;
  const clientStatus = await page.evaluate(async () => {
    try {
      await window.bombomApi.fetcher.patch({
        path: '/members/me',
        body: { nickname: 'invalid..nickname' },
      });
      return 0;
    } catch (error) {
      return error.status;
    }
  });
  assert.equal(
    clientStatus,
    400,
    'actual frontend fetcher must pass CSRF and reach controller validation',
  );
  assert.ok(
    requests
      .slice(beforeRuntime)
      .some((request) => request.method === 'PATCH' && request.csrfHeader),
    'actual frontend must attach fetched CSRF token',
  );

  await context.addCookies([
    {
      name: 'NONE_CONTROL',
      value: 'test',
      domain: 'api.bombom.test',
      path: '/',
      secure: true,
      sameSite: 'None',
    },
  ]);
  await page.goto(evilUrl);
  const external = await page.evaluate(async (url) => {
    try {
      await fetch(url + '/api/v1/members/me', { credentials: 'include' });
      return 'readable';
    } catch {
      return 'blocked';
    }
  }, apiUrl);
  assert.equal(external, 'blocked');
  const externalRequest = requests.find(
    (request) =>
      request.path === '/api/v1/members/me' && request.origin === evilUrl,
  );
  assert.ok(externalRequest, 'cross-site request reached real backend');
  assert.ok(
    !externalRequest.cookies.includes('JSESSIONID_BROWSER_TEST'),
    'Lax cookie must be absent',
  );
  if (engineName !== 'webkit') {
    assert.ok(
      externalRequest.cookies.includes('NONE_CONTROL'),
      'None control cookie must be present to isolate SameSite behavior',
    );
  }

  // 배포 전 남아 있는 SameSite=None 세션도 CORS로 보호되는지 확인한다.
  await context.addCookies([{ ...mainCookie, sameSite: 'None' }]);
  const legacy = await page.evaluate(async (url) => {
    try {
      await fetch(url + '/api/v1/members/me', { credentials: 'include' });
      return 'readable';
    } catch {
      return 'blocked';
    }
  }, apiUrl);
  assert.equal(legacy, 'blocked');
  const legacyRequest = requests
    .filter(
      (request) =>
        request.path === '/api/v1/members/me' && request.origin === evilUrl,
    )
    .at(-1);
  if (engineName !== 'webkit')
    assert.ok(
      legacyRequest.cookies.includes('JSESSIONID_BROWSER_TEST'),
      'legacy None cookie must exercise credentialed CORS denial',
    );
  await context.addCookies([mainCookie]);

  const attackerUrl = `https://attacker.bombom.test:${port}`;
  await page.goto(attackerUrl + '/inject');
  assert.ok(
    !(await context.cookies(apiUrl)).some(
      (cookie) => cookie.name === '__Host-BOMBOM_APPLE_AUTH',
    ),
    'sibling subdomain must not inject Host-prefixed correlation cookie',
  );
  const sameSiteAttack = await page.evaluate(async (url) => {
    try {
      await fetch(url + '/api/v1/members/me', { credentials: 'include' });
      return 'readable';
    } catch {
      return 'blocked';
    }
  }, apiUrl);
  assert.equal(sameSiteAttack, 'blocked');
  const siblingRequest = requests.find(
    (request) =>
      request.path === '/api/v1/members/me' && request.origin === attackerUrl,
  );
  assert.ok(
    siblingRequest.cookies.includes('JSESSIONID_BROWSER_TEST'),
    'same-site sibling sends Lax cookie but must fail CORS',
  );

  // 로컬 IdP가 Apple 형태의 cross-site POST 취소 응답을 보낸다. 실제 Apple에 접속하지 않는다.
  await page.goto(apiUrl + '/oauth2/authorization/apple');
  try {
    await page.waitForURL(
      (url) => url.origin === appUrl && url.pathname === '/login',
      { timeout: 15000 },
    );
  } catch {
    const current = new URL(page.url());
    throw new Error(
      JSON.stringify({
        stage: 'apple-navigation',
        origin: current.origin,
        path: current.pathname,
        queryKeys: [...current.searchParams.keys()],
        requests,
      }),
    );
  }
  const applePost = requests.find(
    (request) =>
      request.path === '/login/sso/verify/success/apple' &&
      request.method === 'POST',
  );
  assert.ok(applePost, 'Apple POST callback reached real backend');
  assert.equal(applePost.origin, idpUrl);
  assert.ok(
    !applePost.cookies.includes('JSESSIONID_BROWSER_TEST'),
    'Apple POST must not send main Lax cookie',
  );
  assert.ok(
    applePost.cookies.includes('__Host-BOMBOM_APPLE_AUTH'),
    'Apple POST sends only correlation cookie',
  );
  assert.ok(
    !(await context.cookies(apiUrl)).some(
      (cookie) => cookie.name === '__Host-BOMBOM_APPLE_AUTH',
    ),
    'temporary cookie is removed',
  );
  assert.equal(new URL(page.url()).searchParams.get('error'), '');
  assert.equal(
    (await context.cookies(apiUrl)).find(
      (cookie) => cookie.name === 'JSESSIONID_BROWSER_TEST',
    )?.value,
    mainCookie.value,
    'cancellation must preserve existing main session cookie',
  );
  const afterCancel = await page.evaluate(
    (url) =>
      fetch(url + '/api/v1/members/me', {
        credentials: 'include',
      }).then((response) => response.status),
    apiUrl,
  );
  assert.equal(
    afterCancel,
    200,
    'existing login remains usable after cancellation',
  );
  // 동일한 외부 POST가 서명/nonce 검증을 거쳐 새로운 로그인 세션을 발급하는지 확인한다.
  idpOutcome = 'success';
  const callbackResponse = page.waitForResponse(
    (response) =>
      response.url() === apiUrl + '/login/sso/verify/success/apple' &&
      response.request().method() === 'POST',
  );
  await page.goto(apiUrl + '/oauth2/authorization/apple');
  assert.equal(
    (await callbackResponse).status(),
    200,
    'signed Apple POST must authenticate',
  );
  await page.waitForURL(apiUrl + '/login/sso/verify/success/apple');
  await page.waitForLoadState();
  const afterLoginCookies = await context.cookies(apiUrl);
  assert.notEqual(
    afterLoginCookies.find(
      (cookie) => cookie.name === 'JSESSIONID_BROWSER_TEST',
    )?.value,
    mainCookie.value,
    'successful cross-site login must rotate main session',
  );
  assert.ok(
    !afterLoginCookies.some(
      (cookie) => cookie.name === '__Host-BOMBOM_APPLE_AUTH',
    ),
    'successful callback removes correlation cookie',
  );
  await page.goto(appUrl);
  assert.equal(
    await page.evaluate(
      (url) =>
        fetch(url + '/api/v1/members/me', { credentials: 'include' }).then(
          (response) => response.status,
        ),
      apiUrl,
    ),
    200,
    'new browser session must authenticate real member API',
  );
  assert.equal(
    await page.evaluate(async () => {
      try {
        await window.bombomApi.fetcher.patch({
          path: '/members/me',
          body: { nickname: 'invalid..nickname' },
        });
      } catch (error) {
        return error.status;
      }
    }),
    400,
    'actual frontend must use fresh CSRF after cross-site login',
  );
  await browser.close();
  browser = null;
  result = {
    passed: true,
    engine: engineName,
    thirdPartyNoneCookieSent: externalRequest.cookies.includes('NONE_CONTROL'),
    checks: [
      'same-site authenticated response',
      'Secure/HttpOnly/Lax session cookie',
      'CSRF missing/invalid/valid',
      'actual frontend runtime CSRF',
      'cross-site response blocked',
      'cross-site main cookie absent',
      'Apple POST correlation cookie',
      'Apple POST main cookie absent',
      'Apple cancellation and cookie cleanup',
      'signed Apple POST login and session rotation',
      'subdomain cookie injection blocked',
      'legacy None session CORS blocked',
      'same-site sibling CORS blocked',
    ],
  };
} finally {
  if (browser) await browser.close();
  for (const socket of tunnelSockets) socket.destroy();
  if (tunnel) await new Promise((resolve) => tunnel.close(resolve));
  if (proxy) {
    proxy.closeAllConnections();
    await new Promise((resolve) => proxy.close(resolve));
  }
  http.globalAgent.destroy();
  https.globalAgent.destroy();
  rmSync(directory, { recursive: true, force: true });
}

process.stdout.write(JSON.stringify(result) + '\n');
