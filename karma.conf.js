var webpackConfig = require('./client/webpack.config.js');

module.exports = function (config) {
  config.set({

    basePath: '',

    frameworks: ['jasmine'],

    webpack: {
      module: webpackConfig.module,
      resolve: webpackConfig.resolve,
      devtool: 'inline-source-map',
      plugins: webpackConfig.plugins
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

    browsers: ['Chrome'],

    junitReporter: {
      outputDir: 'test-output/client'
    },

    singleRun: false
  });
};
