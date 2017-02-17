require('angular');
require('angular-route');
require('angular-mocks');

const context = require.context('.', true, /.+\.spec\*?$/);
context.keys().forEach(context);
module.exports = context;