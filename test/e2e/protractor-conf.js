'use strict';
var ScreenShotReporter = require('protractor-jasmine2-screenshot-reporter');
var webpack = require('webpack');
var config = require('./webpack.config');


exports.config = {

  allScriptsTimeout: 11000,

  capabilities: {
    'browserName': 'chrome',
    'loggingPrefs': {
      'browser': 'WARNING'
    }
  },

  specs: [__dirname + '/.tmp/test.js'],

  framework: 'jasmine2',

  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 10000
  },
  onPrepare: function () {

    jasmine.getEnv().addReporter(new ScreenShotReporter({
      dest: 'test-output',
      captureOnlyFailedSpecs: true
    }));

    var jasmineReporters = require('jasmine-reporters');
    jasmine.getEnv().addReporter(
      new jasmineReporters.JUnitXmlReporter({
        consolidateAll: true,
        filePrefix: 'e2e',
        savePath: 'test-output'
      })
    );

    var disableNgAnimate = function () {
      angular.module('disableNgAnimate', []).run(['$animate', function ($animate) {
        $animate.enabled(false);
      }]);
    };

    browser.addMockModule('disableNgAnimate', disableNgAnimate);

    process.env.PORT = 3001;
    return require('../../build/app').start();
  }
};