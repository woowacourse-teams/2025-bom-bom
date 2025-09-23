const { withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

const NETWORK_CONFIG_PATH = 'res/xml/network_security_config.xml';

/**
 * network_security_config.xml 파일을 생성하는 Config Plugin
 * - android/app/src/main/res/xml/network_security_config.xml 에 자동 생성
 */
function withNetworkSecurityConfigFile(config) {
  return withDangerousMod(config, [
    'android',
    (config) => {
      const filePath = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main',
        NETWORK_CONFIG_PATH,
      );

      const dir = path.dirname(filePath);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }

      const xml = `<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">bombom.news</domain>
  </domain-config>
</network-security-config>`;

      fs.writeFileSync(filePath, xml);
      console.log('✅ network_security_config.xml 생성 완료:', filePath);

      return config;
    },
  ]);
}

module.exports = withNetworkSecurityConfigFile;
