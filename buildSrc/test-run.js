require('source-map-support').install();
const runHelpers = require('./run-helpers');

const args = process.argv;

runHelpers.startJasmineSimple('', args[2], 'build/test-results/jsTest', 'js')
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.log('Jasmine test run failed.');
    process.exit(1);
  });