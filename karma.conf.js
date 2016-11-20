// Karma configuration
// Generated on Tue Jul 01 2014 20:59:56 GMT-0400 (EDT)
var webpackConfig = require('./webpack.config.js');

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

        reporters: ['progress'],

        port: 9876,

        colors: true,

        logLevel: config.LOG_INFO,

        autoWatch: true,

        browsers: ['PhantomJS'
        ],

        singleRun: false
    });
};
