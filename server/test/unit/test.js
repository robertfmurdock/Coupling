// noinspection NpmUsedModulesInstalled
const logging = require('logging');
// noinspection JSUnresolvedVariable, JSUnresolvedFunction
logging.com.zegreatrob.coupling.logging.initializeLogging(false);

// noinspection JSUnresolvedFunction
const context = require.context('.', true, /.+spec\*?$/);
context.keys().forEach(context);
require('../../build/classes/kotlin/test/server_test');