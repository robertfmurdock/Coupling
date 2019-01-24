const runHelpers = require('../test/run-helpers');

const args = process.argv;

runHelpers.startJasmineSimple('', args[2], 'build/test-results/jsTest', 'js');