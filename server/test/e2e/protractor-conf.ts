import {browser, Config} from "protractor";
let ScreenShotReporter = require("protractor-jasmine2-screenshot-reporter");

export let config: Config = {

    allScriptsTimeout: 11000,

    capabilities: {
        'browserName': 'chrome',
        'loggingPrefs': {
            'browser': 'WARNING'
        },
        'seleniumAddress': process.env.SELENIUM_ADDRESS
    },

    specs: [__dirname + '/test.js'],

    framework: 'jasmine2',

    jasmineNodeOpts: {
        showColors: true,
        defaultTimeoutInterval: 10000
    },

    onPrepare: function () {

        const jasmineReporters = require('jasmine-reporters');

        jasmine.getEnv().addReporter(
            new jasmineReporters.JUnitXmlReporter({
                consolidateAll: true,
                filePrefix: 'e2e',
                savePath: 'test-output/e2e'
            })
        );

        jasmine.getEnv().addReporter(new ScreenShotReporter({
            dest: 'build/reports/e2e',
            cleanDestination: true,
            captureOnlyFailedSpecs: true
        }));

        const disableNgAnimate = function () {
            // @ts-ignore
            angular.module('disableNgAnimate', []).run(['$animate', function ($animate) {
                $animate.enabled(false);
            }]);
        };

        browser.addMockModule('disableNgAnimate', disableNgAnimate);
    }
};