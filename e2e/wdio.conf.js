import {HtmlReporter, ReportAggregator} from '@rpii/wdio-html-reporter';

import WDIOReporter from '@wdio/reporter'


// noinspection NpmUsedModulesInstalled
const testLogging = require('coupling-libraries-test-logging');

// noinspection JSUnresolvedFunction
const loggingReporter = new testLogging.com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter();


class CustomReporter extends WDIOReporter {
    constructor(options) {
        super(options)
    }

    onTestStart(test) {
        loggingReporter.startTest(test.fullTitle)
    }

    onTestEnd(test) {
        loggingReporter.endTest(test.fullTitle, test.state, test.errors)
    }
}

const log4js = require('@log4js-node/log4js-api');
const logger = log4js.getLogger('default');
const path = require('path');

const reportDirectory = path.relative('./', process.env.REPORT_DIR) + "/"

const config = {
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
    baseUrl: `${process.env.BASEURL}`,
    waitforTimeout: 6000,
    waitforInterval: 15, //THIS IS INCREDIBLY IMPORTANT FOR PERFORMANCE
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: ['chromedriver'],
    framework: 'jasmine',
    reporters: [
        'dot',
        CustomReporter,
        [HtmlReporter, {
            debug: true,
            outputDir: reportDirectory,
            filename: 'report.html',
            reportTitle: 'Coupling E2E Report',
            showInBrowser: false,
            useOnAfterCommandForScreenshot: true,
            LOG: logger
        }
        ],
    ],
    jasmineOpts: {
        helpers: [],
        defaultTimeoutInterval: 60000,
    },
    beforeSession: async function () {
    },

    afterTest: function (test, context, result) {
        if (result.passed) {
            return;
        }
        const timestamp = new Date().getUTCMilliseconds();
        const filepath = path.join(reportDirectory, 'screenshots/', timestamp + '.png');
        browser.saveScreenshot(filepath);
        process.emit('test:screenshot', filepath);
    },
    onPrepare: function (config, capabilities) {
        let reportAggregator = new ReportAggregator({
            outputDir: reportDirectory,
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
