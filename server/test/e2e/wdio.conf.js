import {HtmlReporter} from '@rpii/wdio-html-reporter';

const log4js = require('@log4js-node/log4js-api');
const logger = log4js.getLogger('default');
const path = require('path');

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
  baseUrl: 'http://localhost:3001',
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
      outputDir: './build/reports/e2e/',
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
    const filepath = path.join('./build/reports/e2e/screenshots/', timestamp + '.png');
    browser.saveScreenshot(filepath);
    process.emit('test:screenshot', filepath);
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

