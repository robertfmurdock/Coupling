"use strict";
var ScreenShotReporter = require('protractor-screenshot-reporter');

exports.config = {

    allScriptsTimeout: 11000,

    capabilities: {
        'browserName': 'chrome'
    },

    // Spec patterns are relative to the current working directly when
    // protractor is called.
    specs: ['*-spec.js'],

    // Options to be passed to Jasmine-node.
    framework: 'jasmine2',

    jasmineNodeOpts: {
        showColors: true,
        defaultTimeoutInterval: 30000
    },
    onPrepare: function() {

        jasmine.getEnv().addReporter(new ScreenShotReporter({
            baseDirectory: '/tmp/screenshots',
            takeScreenShotsOnlyForFailedSpecs: true
        }));

        var disableNgAnimate = function() {
            angular.module('disableNgAnimate', []).run(['$animate', function($animate) {
                $animate.enabled(false);
            }]);
        };

        browser.addMockModule('disableNgAnimate', disableNgAnimate);
    }
};