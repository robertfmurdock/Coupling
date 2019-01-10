var context = require.context('.', true, /.+spec\*?$/);
context.keys().forEach(context);
require('engine_test');