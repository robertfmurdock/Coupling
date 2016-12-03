'use strict';
var ScreenShotReporter = require('protractor-jasmine2-screenshot-reporter');
var webpack = require('webpack');
var config = require('./webpack.config');
var fs = require('fs-extra');
var Promise = require('bluebird');

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
  specs: ['./.tmp/test.js'],

  // Options to be passed to Jasmine-node.
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

    jasmine.getEnv().addReporter({
      jasmineDone: function () {
        fs.removeSync(__dirname + '/.tmp');
      }
    });

    var disableNgAnimate = function () {
      angular.module('disableNgAnimate', []).run(['$animate', function ($animate) {
        $animate.enabled(false);
      }]);
    };

    browser.addMockModule('disableNgAnimate', disableNgAnimate);

    process.env.PORT = 3001;
    return require('../../build/app').start()
      .then(function () {
        return new Promise(function (resolve, reject) {
          webpack(config)
            .run(function (err, stats) {
              console.log(stats.toString('minimal'));
              if (err) {
                reject(err);
              }
              console.log('Starting tests:');
              resolve();
            });
        });
      })
  }
}