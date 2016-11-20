require('angular');
require('angular-mocks');

var context = require.context('.', true, /.+\.spec\*?$/);
context.keys().forEach(context);
module.exports = context;