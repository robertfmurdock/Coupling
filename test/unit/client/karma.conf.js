const webpackConfig = require('./webpack.config');

const chooseBrowsers = function () {
  if (process.env['HEADLESS'] === 'true') {
    return ['PhantomJS'];
  } else {
    return ['Chrome', 'Firefox'];
  }
};

module.exports = function (config) {
  config.set({

    basePath: '../../../',

    frameworks: ['jasmine'],

    webpack: webpackConfig,

    webpackMiddleware: {
      stats: 'minimal'
    },

    files: [
      'node_modules/jquery/dist/jquery.min.js',
      'public/app/build/vendor/vendor.js',
      'test/unit/client/tests.bundle.js',
    ],

    preprocessors: {
      'test/unit/client/tests.bundle.js': ['webpack', 'sourcemap']
    },

    exclude: [],

    reporters: ['progress', 'junit'],

    port: 9876,

    colors: true,

    // logLevel: config.LOG_INFO,

    autoWatch: true,

    browsers: chooseBrowsers(),

    junitReporter: {
      outputDir: 'test-output/client'
    },

    singleRun: false
  });
};
