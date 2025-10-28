// plugins/notification-tools-replace.js
const { withAndroidManifest, AndroidConfig } = require('@expo/config-plugins');

module.exports = function withNotificationToolsReplace(config, props = {}) {
  return withAndroidManifest(config, (config) => {
    const manifest = config.modResults;

    manifest.manifest.$ = manifest.manifest.$ || {};
    if (!manifest.manifest.$['xmlns:tools']) {
      manifest.manifest.$['xmlns:tools'] = 'http://schemas.android.com/tools';
    }

    const application =
      AndroidConfig.Manifest.getMainApplicationOrThrow(manifest);
    application['meta-data'] = application['meta-data'] || [];

    const NAME = 'com.google.firebase.messaging.default_notification_color';
    const RESOURCE = props.resource || '@color/notification_icon_color';

    const existing = application['meta-data'].find(
      (m) => m.$['android:name'] === NAME,
    );

    if (existing) {
      existing.$['android:resource'] = RESOURCE;
      existing.$['tools:replace'] = 'android:resource';
    } else {
      application['meta-data'].push({
        $: {
          'android:name': NAME,
          'android:resource': RESOURCE,
          'tools:replace': 'android:resource',
        },
      });
    }

    return config;
  });
};
