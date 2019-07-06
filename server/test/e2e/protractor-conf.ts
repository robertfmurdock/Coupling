import {browser, By, Config, element} from "protractor";
import e2eHelper from './e2e-help'
import TribeListPage from "./page-objects/TribeListPage";

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

    onPrepare: async function () {
        await browser.waitForAngularEnabled(false);

        browser.baseUrl = 'http://localhost:3001';
        const jasmineReporters = require('jasmine-reporters');

        jasmine.getEnv().addReporter(
            new jasmineReporters.JUnitXmlReporter({
                consolidateAll: true,
                filePrefix: 'e2e',
                savePath: 'build/test-results/e2e'
            })
        );

        jasmine.getEnv().addReporter(new ScreenShotReporter({
            dest: 'build/reports/e2e',
            cleanDestination: true,
            captureOnlyFailedSpecs: true
        }));

        await browser.get('http://localhost:3001');
        await browser.executeScript('window.sessionStorage.setItem(\'animationDisabled\', true)');

        await browser.wait(() => element(By.className('view-frame')).isPresent(), 2000);

        await e2eHelper.clearBrowserLogs();
    }
};