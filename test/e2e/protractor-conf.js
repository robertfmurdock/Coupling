"use strict";
var ScreenShotReporter = require('protractor-jasmine2-screenshot-reporter');

exports.config = {

  allScriptsTimeout: 11000,

  capabilities: {
    'browserName': 'chrome',
    'loggingPrefs': {
      'browser': 'WARNING'
    }
  },

  // Spec patterns are relative to the current working directly when
  // protractor is called.
  specs: ['*-spec.js'],

  // Options to be passed to Jasmine-node.
  framework: 'jasmine2',

  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 5000
  },
  onPrepare: function () {

    jasmine.getEnv().addReporter(new ScreenShotReporter({
      dest: '/tmp/screenshots',
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
  }
};