const webpackConfig = require('./webpack.config');
const url = require('url');

const chooseBrowsers = function () {
  if (process.env.SELENIUM_ADDRESS) {
    return ['remote-chrome'];
  } else {
    return ['Chrome', 'Firefox'];
  }
};

let seleniumAddress = url.parse(process.env.SELENIUM_ADDRESS || '');

const webdriverConfig = {
  hostname: seleniumAddress.hostname,
  port: seleniumAddress.port
};

module.exports = function (config) {
  config.set({

    basePath: '../..',

    frameworks: ['jasmine'],

    webpack: webpackConfig,

    webpackMiddleware: {
      stats: 'minimal'
    },

    files: [
      'client/node_modules/jquery/dist/jquery.min.js',
      'client/build/lib/vendor/vendor.js',
      'client/test/tests.bundle.js',
    ],

    preprocessors: {
      'client/test/tests.bundle.js': ['webpack', 'sourcemap']
    },

    exclude: [],

    reporters: ['progress', 'junit'],

    port: 9876,

    colors: true,

    // logLevel: config.LOG_INFO,

    autoWatch: true,

    customLaunchers: {
      'remote-chrome': {
        base: 'WebDriver',
        config: webdriverConfig,
        browserName: 'chrome',
      }
    },

    browsers: chooseBrowsers(),

    junitReporter: {
      outputDir: 'test-output/client'
    },

    singleRun: false
  });
};
