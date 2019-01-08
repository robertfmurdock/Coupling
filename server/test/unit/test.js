var context = require.context('.', true, /.+spec\*?$/);
context.keys().forEach(context);
require('../../../engine/build/classes/kotlin/js/test/engine_test');