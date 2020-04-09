const clientTest = require('Coupling-client-test');

clientTest.setLogLevel();

require('./enzymeConfig');

const context = require.context('.', true, /.+\.spec\*?$/);
context.keys().forEach(context);

module.exports = context;