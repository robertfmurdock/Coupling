const context = require.context('.', true, /.+\.e2e\*?$/);
context.keys().forEach(context);
// noinspection NpmUsedModulesInstalled
require("Coupling-server-endToEndTest");