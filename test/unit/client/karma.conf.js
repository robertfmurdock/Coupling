var webpackConfig = require('./webpack.config');

module.exports = function (config) {
  config.set({

    basePath: '../../../',

    frameworks: ['jasmine'],

    webpack: webpackConfig,

    webpackMiddleware: {
      stats: 'minimal'
    },

    files: [
      'test/unit/client/tests.bundle.js'
    ],

    preprocessors: {
      'test/unit/client/tests.bundle.js': ['webpack', 'sourcemap']
    },

    exclude: [],

    reporters: ['progress', 'junit'],

    port: 9876,

    colors: true,

    logLevel: config.LOG_INFO,

    autoWatch: true,

    browsers: ['Chrome', 'Firefox'],

    junitReporter: {
      outputDir: 'test-output/client'
    },

    singleRun: false
  });
};
