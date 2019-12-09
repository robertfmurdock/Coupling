// noinspection NpmUsedModulesInstalled
const logging = require('Coupling-logging');
// noinspection JSUnresolvedVariable, JSUnresolvedFunction
logging.com.zegreatrob.coupling.logging.initializeJasmineLogging(false);

// noinspection JSUnresolvedFunction
const context = require.context('.', true, /.+spec\*?$/);
context.keys().forEach(context);
require('Coupling-server-test');