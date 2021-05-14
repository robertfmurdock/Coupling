import {HtmlReporter, ReportAggregator} from '@rpii/wdio-html-reporter';

const log4js = require('@log4js-node/log4js-api');
const logger = log4js.getLogger('default');
const path = require('path');

const e2eBuildDirectory = '../../../../e2e/build'

let config = {
    runner: 'local',
    specs: [
        __dirname + '/test.js'
    ],
    sync: false,
    exclude: [],
    maxInstances: 1,
    capabilities: [{
        maxInstances: 1,
        browserName: 'chrome',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        acceptInsecureCerts: true,
        'goog:chromeOptions': {
            'args': [
                'headless',
                'show-fps-counter=true'
            ]
        },
    }],
    logLevel: 'warn',
    bail: 0,
    baseUrl: `http://localhost:${process.env.PORT}`,
    waitforTimeout: 6000,
    waitforInterval: 15, //THIS IS INCREDIBLY IMPORTANT FOR PERFORMANCE
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: ['chromedriver'],
    framework: 'jasmine',
    reporters: [
        'dot',
        [HtmlReporter, {
            debug: true,
            outputDir: path.join(e2eBuildDirectory, './reports/e2e/'),
            filename: 'report.html',
            reportTitle: 'Coupling E2E Report',
            showInBrowser: false,
            useOnAfterCommandForScreenshot: true,
            LOG: logger
        }
        ],
    ],
    jasmineNodeOpts: {
        helpers: [],
        failFast: true,
        defaultTimeoutInterval: 60000,
    },
    beforeSession: async function () {
        // noinspection NpmUsedModulesInstalled
        const testLogging = require('Coupling-test-logging');

        // noinspection JSUnresolvedFunction
        const loggingReporter = new testLogging.com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter();

        jasmine.getEnv().addReporter(loggingReporter);
    },

    afterTest: function (test, context, result) {
        if (result.passed) {
            return;
        }
        const timestamp = new Date().toISOString();
        const filepath = path.join(e2eBuildDirectory, './reports/e2e/screenshots/', timestamp + '.png');
        browser.saveScreenshot(filepath);
        process.emit('test:screenshot', filepath);
    },
    onPrepare: function (config, capabilities) {
        let reportAggregator = new ReportAggregator({
            outputDir: path.join(e2eBuildDirectory, './reports/e2e/'),
            filename: 'master-report.html',
            reportTitle: 'Master Report',
            browserName: capabilities.browserName,
        });
        reportAggregator.clean();

        global.reportAggregator = reportAggregator;
    },

    onComplete: async function (exitCode, config, capabilities, results) {
        await global.reportAggregator.createReport();
    },

};

if (process.env.SELENIUM_ADDRESS) {
    let url = new URL(process.env.SELENIUM_ADDRESS);
    config.hostname = url.hostname
    config.port = parseInt(url.port)
    config.path = url.pathname
    config.services = []
}

exports.config = config
