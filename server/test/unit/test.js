var context = require.context('.', true, /.+spec\*?$/);
context.keys().forEach(context);
require('../../build/classes/kotlin/test/server_test');