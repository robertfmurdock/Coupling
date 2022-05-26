const url = require('url');

const seleniumAddress = url.parse(process.env.SELENIUM_ADDRESS || '');

const webdriverConfig = {
    hostname: seleniumAddress.hostname,
    port: seleniumAddress.port
};

config.customLaunchers = {
    'remote-chrome': {
        base: 'WebDriver',
        config: webdriverConfig,
        browserName: 'chrome',
    },
    'remote-firefox': {
        base: 'WebDriver',
        config: webdriverConfig,
        browserName: 'firefox',
    }
};

if (process.env.SELENIUM_ADDRESS) {
    config.browsers = ['remote-chrome'];
}

config.webpack.externals = ["fs"]