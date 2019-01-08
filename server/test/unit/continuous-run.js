const runHelpers = require('../../../test/run-helpers');
const forkHelpers = require('../../../test/fork-helpers');
const webpackRunner = require('../../../test/webpackRunner');
const config = require('./webpack.config');

const removeTempDirectory = runHelpers.removeTempDirectory;

function forkJasmine() {
  return forkHelpers.forkJasmine('server/test/unit', '.tmp', 'test.js', __dirname + '/../../../test-output').promise;
}

let testRun = undefined;
webpackRunner.watch(config, function () {
  if (testRun) {
    testRun = testRun
      .then(forkJasmine, function (err) {
        console.log('Fork exited:', err);
      })
  } else {
    testRun = forkJasmine();
  }
});

process.on('SIGINT', function () {
  console.log("Caught interrupt signal");
  removeTempDirectory(__dirname + '/.tmp');
});